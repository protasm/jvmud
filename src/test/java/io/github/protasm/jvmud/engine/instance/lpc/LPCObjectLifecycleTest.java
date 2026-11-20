package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.execution.LPCProgramRevision;
import io.github.protasm.jvmud.language.execution.LPCRuntimeClassLoader;
import io.github.protasm.jvmud.language.execution.LPCRuntimeException;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/** Focused examples of the distinction between stable LPC identity and executable receiver. */
final class LPCObjectLifecycleTest {
    @Test
    void preparingAndInstallingARevisionPreservesIdentityAndLiveState() throws Exception {
        var lifecycle = new LPCObjectLifecycle();
        var firstRevision = revision("I", false);
        Object identity = firstRevision.objectClass().getConstructor().newInstance();
        Map<String, Object> sharedState = new HashMap<>();
        sharedState.put("visitors", 2);
        set(identity, "count", 7);
        set(identity, "data", sharedState);
        set(identity, "reference", identity);

        lifecycle.register(identity);
        lifecycle.register(identity, firstRevision);
        var nextRevision = revision("I", false);
        var prepared = lifecycle.prepareReload(identity, nextRevision);

        assertSame(identity, lifecycle.receiverOf(identity));
        assertSame(firstRevision, lifecycle.revisionOf(identity).orElseThrow());
        assertNotSame(identity, prepared.receiver());
        assertEquals(7, get(prepared.receiver(), "count"));
        assertSame(sharedState, get(prepared.receiver(), "data"));
        assertSame(identity, get(prepared.receiver(), "reference"));

        lifecycle.install(prepared);

        assertSame(identity, prepared.identity());
        assertSame(identity, lifecycle.identityOf(prepared.receiver()));
        assertSame(prepared.receiver(), lifecycle.receiverOf(identity));
        assertSame(nextRevision, lifecycle.revisionOf(identity).orElseThrow());
        lifecycle.register(identity); // Re-registering a runtime name must not undo a reload.
        assertSame(prepared.receiver(), lifecycle.receiverOf(identity));

        var third = lifecycle.prepareReload(identity, revision("I", false));
        lifecycle.install(third);
        assertSame(third.receiver(), lifecycle.receiverOf(prepared.receiver()));
        assertSame(identity, lifecycle.identityOf(third.receiver()));
    }

    @Test
    void changedStateSchemaLeavesTheWorkingRevisionUntouched() throws Exception {
        var lifecycle = new LPCObjectLifecycle();
        var revision = revision("I", false);
        Object identity = revision.objectClass().getConstructor().newInstance();
        set(identity, "count", 11);
        lifecycle.register(identity, revision);

        var error = assertThrows(LPCRuntimeException.class,
                () -> lifecycle.prepareReload(identity, revision("F", false)));

        assertTrue(error.getMessage().contains("live field declarations changed"), error.getMessage());
        assertSame(identity, lifecycle.receiverOf(identity));
        assertSame(revision, lifecycle.revisionOf(identity).orElseThrow());
        assertEquals(11, get(identity, "count"));
    }

    @Test
    void installingAPreparedRevisionRetainsStateChangedSincePreparation() throws Exception {
        var lifecycle = new LPCObjectLifecycle();
        var revision = revision("I", false);
        Object identity = revision.objectClass().getConstructor().newInstance();
        set(identity, "count", 3);
        lifecycle.register(identity, revision);
        var prepared = lifecycle.prepareReload(identity, revision("I", false));
        Map<String, Object> latestData = new HashMap<>();
        latestData.put("visitors", 5);

        set(identity, "count", 4);
        set(identity, "data", latestData);
        lifecycle.install(prepared);

        assertEquals(4, get(lifecycle.receiverOf(identity), "count"));
        assertSame(latestData, get(lifecycle.receiverOf(identity), "data"));
    }

    @Test
    void aFailedJavaAllocationLeavesTheWorkingRevisionUntouched() throws Exception {
        var lifecycle = new LPCObjectLifecycle();
        var revision = revision("I", false);
        Object identity = revision.objectClass().getConstructor().newInstance();
        lifecycle.register(identity, revision);

        var error = assertThrows(LPCRuntimeException.class,
                () -> lifecycle.prepareReload(identity, revision("I", true)));

        assertTrue(error.getCause() instanceof IllegalStateException);
        assertSame(identity, lifecycle.receiverOf(identity));
        assertSame(revision, lifecycle.revisionOf(identity).orElseThrow());
    }

    @Test
    void historicalClosuresShareCurrentStateAcrossNestedCallsAndExceptions() throws Exception {
        var lifecycle = new LPCObjectLifecycle();
        var revision = revision("I", false);
        Object identity = revision.objectClass().getConstructor().newInstance();
        lifecycle.register(identity, revision);
        var second = lifecycle.prepareReload(identity, revision("I", false));
        lifecycle.install(second);
        var third = lifecycle.prepareReload(identity, revision("I", false));
        lifecycle.install(third);
        set(third.receiver(), "count", 20);

        var thrown = assertThrows(IllegalStateException.class, () -> lifecycle.withReceiver(identity, () -> {
            assertSame(identity, lifecycle.receiverOf(identity));
            assertEquals(20, getUnchecked(identity, "count"));
            setUnchecked(identity, "count", 21);
            lifecycle.withReceiver(second.receiver(), () -> {
                assertSame(second.receiver(), lifecycle.receiverOf(identity));
                assertEquals(21, getUnchecked(second.receiver(), "count"));
                setUnchecked(second.receiver(), "count", 22);
                return null;
            });
            assertSame(identity, lifecycle.receiverOf(identity));
            assertEquals(22, getUnchecked(identity, "count"));
            throw new IllegalStateException("closure failure");
        }));

        assertEquals("closure failure", thrown.getMessage());
        assertSame(third.receiver(), lifecycle.receiverOf(identity));
        assertSame(third.revision(), lifecycle.revisionOf(identity).orElseThrow());
        assertEquals(22, get(third.receiver(), "count"));
    }

    @Test
    void activeExecutionAndStalePreparationsCannotReplaceLiveBehavior() throws Exception {
        var lifecycle = new LPCObjectLifecycle();
        var revision = revision("I", false);
        Object identity = revision.objectClass().getConstructor().newInstance();
        lifecycle.register(identity, revision);
        var nextRevision = revision("I", false);
        var prepared = lifecycle.prepareReload(identity, nextRevision);

        lifecycle.withReceiver(identity, () -> {
            assertThrows(IllegalStateException.class, () -> lifecycle.prepareReload(identity, nextRevision));
            assertThrows(IllegalStateException.class, () -> lifecycle.install(prepared));
            return null;
        });
        var competing = lifecycle.prepareReload(identity, nextRevision);
        lifecycle.install(competing);
        assertThrows(IllegalStateException.class, () -> lifecycle.install(prepared));
        assertSame(competing.receiver(), lifecycle.receiverOf(identity));
    }

    @Test
    void removingAnObjectRetainsHistoricalIdentityForDestructionChecks() throws Exception {
        var lifecycle = new LPCObjectLifecycle();
        var revision = revision("I", false);
        Object identity = revision.objectClass().getConstructor().newInstance();
        lifecycle.register(identity, revision);
        var replacement = lifecycle.prepareReload(identity, revision("I", false));
        lifecycle.install(replacement);

        lifecycle.remove(replacement.receiver());

        assertSame(identity, lifecycle.receiverOf(identity));
        assertSame(identity, lifecycle.identityOf(replacement.receiver()));
        assertTrue(lifecycle.revisionOf(identity).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> lifecycle.install(replacement));
    }

    @Test
    void receiverAliasesUseObjectIdentityEvenWhenHostObjectsCompareEqual() {
        var lifecycle = new LPCObjectLifecycle();
        Object first = new EqualHostObject();
        Object second = new EqualHostObject();
        Object unregistered = new EqualHostObject();
        assertEquals(first, second);

        lifecycle.register(first);
        lifecycle.register(second);

        assertSame(first, lifecycle.identityOf(first));
        assertSame(second, lifecycle.identityOf(second));
        assertSame(unregistered, lifecycle.identityOf(unregistered));
        lifecycle.remove(first);
        assertSame(second, lifecycle.receiverOf(second));
    }

    private static final class EqualHostObject {
        @Override
        public boolean equals(Object other) {
            return other instanceof EqualHostObject;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    private static void set(Object object, String field, Object value) throws ReflectiveOperationException {
        object.getClass().getField(field).set(object, value);
    }

    private static Object get(Object object, String field) throws ReflectiveOperationException {
        return object.getClass().getField(field).get(object);
    }

    private static void setUnchecked(Object object, String field, Object value) {
        try {
            set(object, field, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Object getUnchecked(Object object, String field) {
        try {
            return get(object, field);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /** Generates identical binary names in independent loaders, as real LPC revisions do. */
    private static LPCProgramRevision revision(String countDescriptor, boolean brokenConstructor) {
        String name = "reload/room";
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V25, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
        writer.visitField(Opcodes.ACC_PUBLIC, "count", countDescriptor, null, null).visitEnd();
        writer.visitField(Opcodes.ACC_PUBLIC, "data", "Ljava/util/Map;", null, null).visitEnd();
        writer.visitField(Opcodes.ACC_PUBLIC, "reference", "Ljava/lang/Object;", null, null).visitEnd();
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
                "$lpc$initialized", "Z", null, null).visitEnd();
        var constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        if (brokenConstructor) {
            constructor.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
            constructor.visitInsn(Opcodes.DUP);
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    "java/lang/IllegalStateException", "<init>", "()V", false);
            constructor.visitInsn(Opcodes.ATHROW);
        } else {
            constructor.visitInsn(Opcodes.RETURN);
        }
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();
        writer.visitEnd();
        var loader = new LPCRuntimeClassLoader(LPCObjectLifecycleTest.class.getClassLoader());
        return new LPCProgramRevision("reload/room", loader.defineClass(name, writer.toByteArray()));
    }
}
