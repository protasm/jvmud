package io.github.protasm.jvmud.language.compiler;

import io.github.protasm.jvmud.language.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.language.compiler.preprocessor.IncludeResolver;
import io.github.protasm.jvmud.language.compiler.preprocessor.Preprocessor;
import io.github.protasm.jvmud.language.compiler.token.TokenList;
import io.github.protasm.jvmud.language.efun.EfunSignature;

import java.nio.file.Path;

/**
 * Read-only services used while compiling LPC source.
 *
 * <p>Compilation can inspect sources, compatibility policy and callable signatures. It cannot
 * instantiate game objects, open sessions, mutate a world or connect to storage. A hosted instance
 * assembles this environment before asking the compiler to analyze its mudlib.</p>
 */
public interface LPCCompilationEnvironment {
    IncludeResolver includeResolver();

    Preprocessor newPreprocessor();

    boolean dynamicTypes();

    boolean transpileImplicitSelfCalls();

    /** Returns the native function name selected for a mudlib-visible alias. */
    String engineFunctionName(String name);

    /** Tests name presence independently of call arity, including mudlib globals. */
    boolean hasFunctionNamed(String name);

    /** Resolves a mudlib-visible signature, honoring global-function shadowing. */
    EfunSignature resolveFunctionSignature(String name, int arity);

    /** Resolves a native signature without consulting mudlib globals. */
    EfunSignature resolveEngineFunctionSignature(String name, int arity);

    TokenList transpileSourceTokens(Path sourcePath, TokenList tokens);

    void transpileSourceLocals(Path sourcePath, ASTObject object);
}
