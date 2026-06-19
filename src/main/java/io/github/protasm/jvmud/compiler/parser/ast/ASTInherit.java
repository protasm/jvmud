package io.github.protasm.jvmud.compiler.parser.ast;

public final class ASTInherit extends ASTNode {
    private final String path;
    private final boolean virtual;

    public ASTInherit(int line, String path) {
        this(line, path, false);
    }

    public ASTInherit(int line, String path, boolean virtual) {
        super(line);
        this.path = path;
        this.virtual = virtual;
    }

    public String path() {
        return path;
    }

    public boolean isVirtual() {
        return virtual;
    }
}
