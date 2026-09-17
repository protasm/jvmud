package io.github.protasm.jvmud.compiler.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EntityAliasTest {
    @ParameterizedTest
    @ValueSource(strings = {"destroy", "rename", "clear"})
    void replacingAliasSurvivesCleanupOfPreviousOwner(String cleanup) {
        RuntimeContext context = new RuntimeContext(null);
        Object oldPlayer = new Object();
        Object newPlayer = new Object();
        context.bindEntityAlias(oldPlayer, "living", "Solver");
        context.bindEntityAlias(oldPlayer, "other", "retained");
        context.bindEntityAlias(newPlayer, "living", "solver");
        assertEquals(0, context.entityHasAlias(oldPlayer, "living"));

        switch (cleanup) {
            case "destroy" -> context.destructObject(oldPlayer);
            case "rename" -> context.bindEntityAlias(oldPlayer, "living", "former");
            case "clear" -> context.bindEntityAlias(oldPlayer, "living", null);
        }

        assertSame(newPlayer, context.findEntityAlias("living", "solver"));
        assertEquals(1, context.entityHasAlias(newPlayer, "living"));
        if (!cleanup.equals("destroy")) {
            assertSame(oldPlayer, context.findEntityAlias("other", "retained"));
        }
        context.destructObject(newPlayer);
        assertNull(context.findEntityAlias("living", "solver"));
    }
}
