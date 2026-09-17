package io.github.protasm.jvmud.instance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Explicit mudlib root and relative manifest path supplied to the engine. */
public record MudlibSpec(Path root, String configPath) {
    /** Normalizes the root and requires a manifest within that root. */
    public MudlibSpec {
        root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        Objects.requireNonNull(configPath, "configPath");
        Path config = Path.of(configPath);
        if (config.isAbsolute() || !root.resolve(config).normalize().startsWith(root)) {
            throw new IllegalArgumentException("Manifest must be relative to its mudlib root.");
        }
    }

    /** Resolves the conventional jvmud directory, or uses the manifest's parent as the root. */
    public static MudlibSpec fromConfig(Path configFile) {
        Path config = configFile.toAbsolutePath().normalize();
        if (!Files.isRegularFile(config)) {
            throw new IllegalArgumentException("Mudlib config file not found: " + config);
        }
        Path directory = config.getParent();
        Path root = "jvmud".equals(directory.getFileName().toString()) ? directory.getParent() : directory;
        return new MudlibSpec(root, root.relativize(config).toString().replace('\\', '/'));
    }
}
