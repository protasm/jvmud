package io.github.protasm.jvmud.execution.application.worker;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Builds an OS-enforced worker boundary. Unsupported hosts fail closed rather than launching an unsandboxed mudlib. */
public final class WorkerSandbox {
    private WorkerSandbox() {}

    /** Allows the JRE and application classes as read-only inputs, and only this mudlib and scratch as writable filesystems. */
    public static List<String> command(Path root, Path scratch, Path privateState, List<String> javaCommand, List<Path> classpath) throws IOException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        Path jre = Path.of(System.getProperty("java.home")).toRealPath();
        Set<Path> reads = new LinkedHashSet<>(classpath);
        reads.add(jre);
        if (os.contains("mac")) {
            if (!Files.isExecutable(Path.of("/usr/bin/sandbox-exec"))) throw new IOException("macOS sandbox-exec is required for mudlib workers.");
            List<Path> writable = List.of(root.toRealPath(), scratch.toRealPath());
            List<Path> readable = new ArrayList<>();
            for (Path read : reads) readable.add(read.toRealPath());
            readable.addAll(writable);
            String readPaths = readable.stream().map(p -> "(subpath " + quote(p.toString()) + ")").collect(java.util.stream.Collectors.joining(" "));
            String writePaths = writable.stream().map(p -> "(subpath " + quote(p.toString()) + ")").collect(java.util.stream.Collectors.joining(" "));
            // dyld needs to read the root directory itself when locating its shared cache.
            // File metadata remains visible, but file contents outside these inputs are denied.
            StringBuilder profile = new StringBuilder("(version 1)\n(allow default)\n");
            profile.append("(deny file-read-data (require-not (require-any (literal \"/\")")
                    .append(" (subpath \"/System\") (subpath \"/usr/lib\") (subpath \"/usr/share\")")
                    .append(" (subpath \"/Library/Apple\") (subpath \"/private/etc\") (subpath \"/private/var/db\") (subpath \"/dev\") ")
                    .append(readPaths).append(")))\n");
            profile.append("(deny file-read* file-write* (subpath ").append(quote(privateState.toRealPath().toString())).append("))\n");
            profile.append("(deny file-write* (require-not (require-any (literal \"/dev/null\") ").append(writePaths).append(")))\n");
            profile.append("(deny process-fork process-info-setcontrol)\n")
                    .append("(deny signal (require-not (target self)))\n")
                    .append("(deny process-exec (require-not (literal ").append(quote(Path.of(javaCommand.getFirst()).toRealPath().toString())).append(")))\n")
                    .append("(deny network-outbound)\n")
                    .append("(deny network-bind network-inbound (require-not (local ip \"localhost:*\")))\n");
            List<String> command = new ArrayList<>(List.of("/usr/bin/sandbox-exec", "-p", profile.toString()));
            command.addAll(javaCommand); return command;
        }
        if (os.contains("linux")) {
            Path bwrap = Path.of("/usr/bin/bwrap");
            if (!Files.isExecutable(bwrap)) throw new IOException("Linux mudlib sandboxing requires bubblewrap (/usr/bin/bwrap) and enabled user namespaces.");
            List<String> command = new ArrayList<>(List.of(bwrap.toString(), "--die-with-parent", "--new-session",
                    "--unshare-user", "--unshare-pid", "--unshare-ipc", "--unshare-uts", "--unshare-cgroup-try",
                    "--cap-drop", "ALL", "--proc", "/proc", "--dev", "/dev", "--tmpfs", "/tmp"));
            for (String system : List.of("/usr", "/lib", "/lib64", "/etc")) {
                if (Files.exists(Path.of(system))) command.addAll(List.of("--ro-bind", system, system));
            }
            for (Path read : reads) {
                String path = read.toRealPath().toString(); command.addAll(List.of("--ro-bind", path, path));
            }
            for (Path write : List.of(root.toRealPath(), scratch.toRealPath())) {
                command.addAll(List.of("--bind", write.toString(), write.toString()));
            }
            command.addAll(List.of("--tmpfs", privateState.toRealPath().toString()));
            command.addAll(List.of("--chdir", root.toRealPath().toString(), "--")); command.addAll(javaCommand); return command;
        }
        throw new IOException("No worker sandbox is implemented for " + System.getProperty("os.name") + ".");
    }

    private static String quote(String text) { return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
}
