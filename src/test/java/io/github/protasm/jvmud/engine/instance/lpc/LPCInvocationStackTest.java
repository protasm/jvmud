package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.execution.LPCProgramRevision;

import org.junit.jupiter.api.Test;

/** Exercises lexical identity and scope restoration independently of world or command services. */
final class LPCInvocationStackTest {
    private final LPCObjectLifecycle lifecycle = new LPCObjectLifecycle();
    private final LPCInvocationStack stack = new LPCInvocationStack(lifecycle);

    @Test
    void nestedScopesIdentifyTheirCallerAndRestoreItOnReturn() {
        Object caller = new Object();
        Object callee = new Object();
        assertNull(stack.currentObject());
        assertNull(stack.previousObject());
        assertFalse(stack.isExecuting(caller));

        assertEquals("result", stack.withCurrentObject(caller, () -> {
            assertSame(caller, stack.currentObject());
            assertSame(caller, stack.previousObject());
            assertEquals(7, stack.withCurrentObject(callee, () -> {
                assertSame(callee, stack.currentObject());
                assertSame(caller, stack.previousObject());
                assertTrue(stack.isExecuting(caller));
                assertTrue(stack.isExecuting(callee));
                return 7;
            }));
            assertSame(caller, stack.currentObject());
            assertFalse(stack.isExecuting(callee));
            return "result";
        }));
        assertNull(stack.currentObject());
        assertFalse(stack.isExecuting(caller));
    }

    @Test
    void failedNestedCallsRestoreEveryEnclosingScope() {
        Object caller = new Object();
        Object callee = new Object();
        IllegalStateException failure = new IllegalStateException("authored failure");
        stack.withCurrentObject(caller, () -> {
            assertSame(failure, assertThrows(IllegalStateException.class, () ->
                    stack.withCurrentObject(callee, () -> { throw failure; })));
            assertSame(caller, stack.currentObject());
            assertSame(caller, stack.previousObject());
            return null;
        });
        assertNull(stack.currentObject());
        assertNull(stack.previousObject());
    }

    @Test
    void unbalancedManualScopeRetainsItsExplicitError() {
        IllegalStateException failure = assertThrows(IllegalStateException.class, stack::pop);
        assertEquals("No current LPC object is available to pop.", failure.getMessage());
    }

    @Test
    void replacementAndHistoricalReceiversShareOneExecutingIdentity() {
        Behavior identity = new Behavior();
        LPCProgramRevision revision = new LPCProgramRevision("stack/behavior", Behavior.class);
        lifecycle.register(identity, revision);
        var replacement = lifecycle.prepareReload(identity, revision);
        lifecycle.install(replacement);
        var latest = lifecycle.prepareReload(identity, revision);
        lifecycle.install(latest);

        stack.withCurrentObject(latest.receiver(), () -> {
            assertSame(identity, stack.currentObject());
            assertTrue(stack.isExecuting(identity));
            assertTrue(stack.isExecuting(replacement.receiver()));
            return lifecycle.withReceiver(replacement.receiver(), () ->
                    stack.withCurrentObject(replacement.receiver(), () -> {
                        assertSame(identity, stack.currentObject());
                        assertSame(identity, stack.previousObject());
                        assertTrue(stack.isExecuting(latest.receiver()));
                        return null;
                    }));
        });

        assertSame(latest.receiver(), lifecycle.receiverOf(identity));
        assertFalse(stack.isExecuting(identity));
        assertNull(stack.currentObject());
    }

    /** A materializable behavior with an unchanged field layout across receiver replacements. */
    public static final class Behavior {
        public Behavior() {}
    }
}
