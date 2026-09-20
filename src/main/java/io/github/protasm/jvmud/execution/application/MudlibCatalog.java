package io.github.protasm.jvmud.execution.application;

import io.github.protasm.jvmud.execution.instance.MudlibSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Maps administrator-visible names to manifests within a host-configured mudlib directory. */
final class MudlibCatalog {
    private final Path directory;

    /** Declares the directory without requiring any installed mudlibs at engine startup. */
    MudlibCatalog(Path directory) { this.directory = directory.toAbsolutePath().normalize(); }

    /** Lists only names whose conventional manifests resolve within the configured directory. */
    List<String> names() throws IOException {
        if (!Files.exists(directory)) return List.of();
        try (var entries = Files.list(directory)) {
            return entries.map(path -> path.getFileName().toString()).filter(this::available).sorted().toList();
        }
    }

    /** Resolves one name, rejecting path syntax and symlinks outside its permitted tree. */
    MudlibSpec resolve(String name) throws IOException {
        if (!name.matches("[A-Za-z0-9_-]{1,64}"))
            throw new IllegalArgumentException("Use a mudlib name, not a filesystem path. Type available to list mudlibs.");
        MudlibSpec spec = new MudlibSpec(directory.resolve(name), "jvmud/" + name + ".config");
        try { validate(spec); }
        catch (IOException e) { throw new IOException("Mudlib '" + name + "' is unavailable in the configured mudlib directory.", e); }
        return spec;
    }

    /** Rechecks stored manifests before administrative restarts, including changed symlink targets. */
    void validate(MudlibSpec spec) throws IOException {
        Path catalog = directory.toRealPath();
        Path root = spec.root().toRealPath();
        Path manifest = root.resolve(spec.configPath()).toRealPath();
        if (root.equals(catalog) || !root.startsWith(catalog) || !manifest.startsWith(root)
                || !Files.isDirectory(root) || !Files.isRegularFile(manifest))
            throw new IOException("Mudlib files must remain within the configured mudlib directory and their own tree.");
    }

    private boolean available(String name) {
        try { resolve(name); return true; }
        catch (IOException | IllegalArgumentException e) { return false; }
    }
}
