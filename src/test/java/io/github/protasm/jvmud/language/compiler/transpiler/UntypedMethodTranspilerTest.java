package io.github.protasm.jvmud.language.compiler.transpiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.protasm.jvmud.language.compiler.scanner.Scanner;
import io.github.protasm.jvmud.language.compiler.token.Token;
import io.github.protasm.jvmud.language.compiler.token.TokenList;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks token insertion and provenance while preserving already typed source. */
class UntypedMethodTranspilerTest {
    @TempDir Path temp;

    @Test void preservesTypedCodeBodiesAndOriginalLocations() {
        String source = """
                // fake(arg) { }
                string label = "fake(arg) { }";
                static identity(arg) { return arg; }
                int typed(int value) { return value; }
                """;
        TokenList original = new Scanner().scan(source);
        TokenList transformed = new UntypedMethodTranspiler().transpile(original);
        int added = 0, index = 0;
        for (int i = 0; i < transformed.size(); i++) {
            Token<?> token = transformed.get(i);
            if (token == original.get(index)) index++;
            else {
                assertEquals("mixed", token.lexeme());
                assertEquals(original.get(index).span(), token.span());
                assertEquals(3, token.line());
                added++;
            }
        }
        assertEquals(original.size(), index);
        assertEquals(2, added);
        assertEquals(transformed.toString(), new UntypedMethodTranspiler().transpile(transformed).toString());
    }

}
