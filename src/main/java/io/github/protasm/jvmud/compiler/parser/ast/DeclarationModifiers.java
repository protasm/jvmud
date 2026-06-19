package io.github.protasm.jvmud.compiler.parser.ast;

import java.util.Objects;

public final class DeclarationModifiers {
    public enum Visibility {
        DEFAULT,
        PUBLIC,
        PRIVATE,
        PROTECTED
    }

    public static final DeclarationModifiers NONE =
            new DeclarationModifiers(Visibility.DEFAULT, false, false, false, false, false);

    private final Visibility visibility;
    private final boolean staticModifier;
    private final boolean nomask;
    private final boolean varargs;
    private final boolean nosave;
    private final boolean deprecated;

    public DeclarationModifiers(
            Visibility visibility,
            boolean staticModifier,
            boolean nomask,
            boolean varargs,
            boolean nosave,
            boolean deprecated) {
        this.visibility = Objects.requireNonNull(visibility, "visibility");
        this.staticModifier = staticModifier;
        this.nomask = nomask;
        this.varargs = varargs;
        this.nosave = nosave;
        this.deprecated = deprecated;
    }

    public Visibility visibility() {
        return visibility;
    }

    public boolean isStatic() {
        return staticModifier;
    }

    public boolean isNomask() {
        return nomask;
    }

    public boolean isVarargs() {
        return varargs;
    }

    public boolean isNosave() {
        return nosave;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public boolean isPublic() {
        return visibility == Visibility.PUBLIC;
    }

    public boolean isPrivate() {
        return visibility == Visibility.PRIVATE;
    }

    public boolean isProtected() {
        return visibility == Visibility.PROTECTED;
    }
}
