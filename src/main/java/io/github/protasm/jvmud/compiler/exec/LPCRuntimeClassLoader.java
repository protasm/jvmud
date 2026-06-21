package io.github.protasm.jvmud.compiler.exec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

final class LPCRuntimeClassLoader extends ClassLoader {
    private static final String DUMP_CLASSES_PROPERTY = "jvmud.dumpGeneratedClasses";
    private final Map<String, Class<?>> definedClasses = new HashMap<>();

    LPCRuntimeClassLoader(ClassLoader parent) {
        super(parent);
    }

    synchronized Class<?> defineClass(String internalName, byte[] bytecode) {
        String binaryName = toBinaryName(internalName);
        Class<?> existing = definedClasses.get(binaryName);
        if (existing != null) {
            return existing;
        }

        dumpGeneratedClass(internalName, bytecode);
        Class<?> defined = defineClass(binaryName, bytecode, 0, bytecode.length);
        definedClasses.put(binaryName, defined);
        return defined;
    }

    synchronized boolean isDefined(String internalName) {
        return definedClasses.containsKey(toBinaryName(internalName));
    }

    @Override
    protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> defined = definedClasses.get(name);
        if (defined != null) {
            return defined;
        }
        throw new ClassNotFoundException(name);
    }

    private String toBinaryName(String internalName) {
        return internalName.replace('/', '.');
    }

    /** Writes generated LPC bytecode when troubleshooting compiler/runtime behavior. */
    private void dumpGeneratedClass(String internalName, byte[] bytecode) {
        String dumpDirectory = System.getProperty(DUMP_CLASSES_PROPERTY);
        if (dumpDirectory == null || dumpDirectory.isBlank()) {
            return;
        }

        Path output = Path.of(dumpDirectory).resolve(internalName + ".class");
        try {
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(output, bytecode);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to dump generated LPC class " + internalName, e);
        }
    }
}
