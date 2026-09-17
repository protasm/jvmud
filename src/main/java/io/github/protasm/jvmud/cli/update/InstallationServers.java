package io.github.protasm.jvmud.cli.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.*;

/** Private restart records for servers launched from an extracted distribution. */
public final class InstallationServers implements AutoCloseable {
    static final ObjectMapper JSON = new ObjectMapper();
    private final Path record;
    private final Map<String, Object> values;

    private InstallationServers(Path record, Map<String, Object> values) {
        this.record = record;
        this.values = values;
    }

    /** Records engine launch arguments and its log root so an installation update can restart it. */
    public static InstallationServers register(String[] args, Path engineRoot) throws IOException {
        String configured = System.getenv("JVMUD_INSTALL_ROOT");
        if (configured == null) return new InstallationServers(null, Map.of());
        Path root = Path.of(configured).toRealPath();
        Path lockPath = lockPath(root);
        boolean updateChild = Long.toString(ProcessHandle.current().parent().map(ProcessHandle::pid).orElse(-1L))
                .equals(System.getenv("JVMUD_UPDATE_OWNER"));
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = updateChild ? null : channel.tryLock()) {
            if (!updateChild && lock == null) throw new IOException("This installation is being updated; try again after it finishes.");
            if (!updateChild && Files.exists(root.resolve(".jvmud/update-in-progress.json")))
                throw new IOException("An interrupted update needs recovery; see .jvmud/update-in-progress.json.");
            Path directory = root.resolve(".jvmud/servers");
            Files.createDirectories(directory);
            Files.setPosixFilePermissions(root.resolve(".jvmud"), PosixFilePermissions.fromString("rwx------"));
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwx------"));
            ProcessHandle self = ProcessHandle.current();
            Map<String, String> javaEnvironment = new LinkedHashMap<>();
            for (String key : List.of("JAVA_HOME", "JVMUD_JAVA_HOME", "JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS")) {
                if (System.getenv(key) != null) javaEnvironment.put(key, System.getenv(key));
            }
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("pid", self.pid());
            values.put("started", self.info().startInstant().orElseThrow().toString());
            values.put("cwd", Path.of("").toAbsolutePath().toString());
            values.put("args", List.of(args));
            values.put("javaEnvironment", javaEnvironment);
            values.put("engineRoot", engineRoot.toAbsolutePath().normalize().toString());
            values.put("state", "starting");
            InstallationServers result = new InstallationServers(directory.resolve(self.pid() + ".json"), values);
            result.write();
            return result;
        }
    }

    public synchronized void ready() throws IOException {
        if (record != null) { values.put("state", "ready"); write(); }
    }

    @Override public synchronized void close() throws IOException {
        if (record != null) { values.put("state", "stopped"); write(); }
    }

    private void write() throws IOException {
        Path temporary = Files.createTempFile(record.getParent(), ".server-", ".tmp");
        try {
            Files.setPosixFilePermissions(temporary, PosixFilePermissions.fromString("rw-------"));
            JSON.writeValue(temporary.toFile(), values);
            Files.move(temporary, record, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally { Files.deleteIfExists(temporary); }
    }

    static Path lockPath(Path root) throws IOException {
        Path directory = root.resolve(".jvmud");
        Files.createDirectories(directory);
        return directory.resolve("update.lock");
    }

    static boolean isSameProcess(long pid, String started) {
        return ProcessHandle.of(pid).filter(ProcessHandle::isAlive)
                .flatMap(p -> p.info().startInstant()).map(Instant::toString).filter(started::equals).isPresent();
    }
}
