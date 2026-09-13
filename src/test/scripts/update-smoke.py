#!/usr/bin/env python3
"""Exercise real distribution update, preservation, backup, shutdown saves, and rollback."""
import hashlib
import http.server
import importlib.util
import json
import os
from pathlib import Path
import shutil
import socket
import subprocess
import sys
import tarfile
import tempfile
import threading
import time

sys.dont_write_bytecode = True

spec = importlib.util.spec_from_file_location('dist', Path(__file__).with_name('distribution-smoke.py'))
dist = importlib.util.module_from_spec(spec); spec.loader.exec_module(dist)

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def wait_ready(root, pid):
    record = root / f'.jvmud/servers/{pid}.json'
    end = time.monotonic() + 40
    while time.monotonic() < end:
        if record.exists() and json.loads(record.read_text())['state'] == 'ready': return
        time.sleep(.1)
    raise AssertionError(f'Server {pid} never registered ready')

def live_records(root):
    result=[]
    for path in (root / '.jvmud/servers').glob('*.json'):
        value=json.loads(path.read_text())
        if value['state']=='ready': result.append(value)
    return result

def main():
    archive=Path(sys.argv[1]).resolve()
    with tempfile.TemporaryDirectory(prefix='jvmud update test ') as temporary:
        work=Path(temporary)
        with tarfile.open(archive) as tar: tar.extractall(work / 'installed', filter='data')
        root,=(work / 'installed').iterdir()
        with tarfile.open(archive) as tar: tar.extractall(work / 'served', filter='data')
        fresh,=(work / 'served').iterdir()
        version=json.loads((fresh/'metadata/update-index.json').read_text())['version']
        installed=json.loads((root/'metadata/update-index.json').read_text()); installed['version']='0.1.0-test-old'
        # Simulate an unchanged old adapter that this release replaces.
        adapter='mudlibs/lp245/jvmud/mfuns.c'
        with (root/adapter).open('a') as f:f.write('\n// Previous shipped bridge revision.\n')
        installed['adapters'][adapter]=sha(root/adapter)
        # A hash-only old release with a real vendor configuration change.
        config_name='mudlibs/lp245/jvmud/lp245.config'
        with (root/config_name).open('a') as f:f.write('\n# Old vendor default\nplayer_prompt = "> "\n')
        installed['adapters'][config_name]=sha(root/config_name)
        installed.pop('configBaselines', None)
        documentation='mudlibs/lp245/jvmud/README.md'
        with (root/documentation).open('a') as f:f.write('\nOld vendor documentation.\n')
        installed['adapters'][documentation]=sha(root/documentation)
        (root/'metadata/update-index.json').write_text(json.dumps(installed))
        # Only the indexed config is needed to authenticate this legacy baseline.
        with tarfile.open(work/'jvmud-0.1.0-test-old-bin.tar.gz','w:gz') as tar:
            tar.add(root/config_name,arcname='jvmud-0.1.0-test-old/'+config_name)
        with (root/documentation).open('a') as f:f.write('My local documentation.\n')
        local_documentation=(root/documentation).read_bytes()
        config=root/'mudlibs/lp245/jvmud/lp245.config'
        with config.open('a') as f:f.write('\n# My local configuration\n')
        custom=root/'mudlibs/lp245/room/local-area.c'; custom.write_text('local world content')
        local_log=root/'mudlibs/lp245/jvmud/log/local.log'; local_log.parent.mkdir(exist_ok=True); local_log.write_text('keep my log')
        expected_config=config.read_bytes()
        served_archive=work/'release.tar.gz'; shutil.copy2(archive,served_archive)
        manifest=work/'latest.json'
        target=json.loads((root/'metadata/runtime.json').read_text())['target'] if (root/'metadata/runtime.json').exists() else 'bin'
        def publish(digest):manifest.write_text(json.dumps({'version':version,'artifacts':{target:{'url':'release.tar.gz','sha256':digest}}}))
        class Handler(http.server.SimpleHTTPRequestHandler):
            def __init__(self,*args,**kwargs): super().__init__(*args,directory=str(work),**kwargs)
            def log_message(self,*args):pass
        web_server=http.server.ThreadingHTTPServer(('127.0.0.1',0),Handler)
        threading.Thread(target=web_server.serve_forever,daemon=True).start()
        url=f'http://127.0.0.1:{web_server.server_port}/latest.json'
        ports=[dist.free_port(),dist.free_port()]
        while ports[0]==ports[1]:ports[1]=dist.free_port()
        processes=[]; outputs=[]
        try:
            for world,port in zip(['smallmercies','lp245'],ports):
                output=(work/f'{world}-launcher.log').open('w');outputs.append(output)
                processes.append(subprocess.Popen([str(root/'scripts/jvmud-start'),'--port',str(port),world],cwd=root,stdout=output,stderr=subprocess.STDOUT))
                wait_ready(root,processes[-1].pid)
                # Reap stopped children just as a launching shell would.
                threading.Thread(target=processes[-1].wait, daemon=True).start()
            with socket.create_connection(('127.0.0.1',ports[1])) as player:
                player.settimeout(.5)
                dist.until(player,'What is your name: ')
                for text,marker in [('updatetest','Password: '),('secret123','Password: (again) '),('secret123','Please enter your email address'),('none','Are you, male, female or other'),('o','Welcome, Creature!'),('south','You are at an open green place'),('west','An old humpbacked bridge.'),('get money','Ok.')]:
                    dist.command(player,text,marker)
                save_path=root/'mudlibs/lp245/players/updatetest.o'
                before_save=save_path.read_bytes() if save_path.exists() else None
                args=[str(root/'scripts/jvmud-update'),'--manifest',url]
                publish('0'*64)
                failed=subprocess.run(args,cwd=root,text=True,capture_output=True,timeout=90)
                assert failed.returncode!=0 and 'SHA-256 mismatch' in failed.stderr,failed.stdout+failed.stderr
                assert all(p.poll() is None for p in processes),'Bad download stopped a server'
                publish(sha(served_archive))
                updated=subprocess.run(args,cwd=root,text=True,capture_output=True,timeout=150)
                assert updated.returncode==0,updated.stdout+updated.stderr
            for p in processes:p.wait(timeout=10)
            assert len(live_records(root))==2
            assert config.read_bytes()==(fresh/config_name).read_bytes()
            assert custom.read_text()=='local world content'
            assert local_log.read_text()=='keep my log'
            assert sha(root/adapter)==sha(fresh/adapter)
            backups=list((root.parent/'backup').iterdir()); assert len(backups)==1
            assert (backups[0]/custom.relative_to(root)).read_text()=='local world content'
            assert (backups[0]/config_name).read_bytes()==expected_config
            assert (backups[0]/documentation).read_bytes()==local_documentation
            assert (root/documentation).read_bytes()==(fresh/documentation).read_bytes()
            expected_config=config.read_bytes()
            save=root/'mudlibs/lp245/players/updatetest.o'
            assert save.exists(),'Connected player was not saved before backup'
            assert save.read_bytes()!=before_save,'Shutdown did not persist the live player changes'
            assert save.read_bytes()==(backups[0]/save.relative_to(root)).read_bytes(),'Backup missed the shutdown save'
            assert all((root/f'mudlibs/{world}/jvmud/log/server-{port}.log').exists() for world,port in zip(['smallmercies','lp245'],ports))
            assert not list(root.glob('*.log'))
            print('PASS: bad download leaves two servers running; update saves players, backs up beside install, preserves world/config/logs, updates bridge, and restarts both worlds')

            # Force a restart failure and verify rollback of engine files and server recovery.
            installed=json.loads((root/'metadata/update-index.json').read_text());installed['version']='0.1.0-test-rollback'
            (root/'metadata/update-index.json').write_text(json.dumps(installed))
            (fresh/'scripts/jvmud-start').write_text('#!/bin/sh\nexit 7\n')
            with tarfile.open(served_archive,'w:gz') as tar:tar.add(fresh,arcname=fresh.name)
            publish(sha(served_archive))
            rolled=subprocess.run([str(root/'scripts/jvmud-update'),'--manifest',url],cwd=root,text=True,capture_output=True,timeout=150)
            assert rolled.returncode!=0 and 'Previous JVMud files restored' in rolled.stderr,rolled.stdout+rolled.stderr
            assert len(live_records(root))==2
            assert not (root/'.jvmud/update-in-progress.json').exists()
            assert json.loads((root/'metadata/update-index.json').read_text())['version']=='0.1.0-test-rollback'
            assert custom.read_text()=='local world content' and config.read_bytes()==expected_config
            print('PASS: failed restart restores old engine, retains backup and game content, and restarts both original servers')
        finally:
            for record in live_records(root):
                try:os.kill(record['pid'],15)
                except ProcessLookupError:pass
            for p in processes:
                if p.poll() is None:p.terminate()
            for p in processes:
                try:p.wait(timeout=10)
                except subprocess.TimeoutExpired:p.kill();p.wait()
            time.sleep(1)
            for output in outputs:output.close()
            web_server.shutdown()

if __name__=='__main__': main()
