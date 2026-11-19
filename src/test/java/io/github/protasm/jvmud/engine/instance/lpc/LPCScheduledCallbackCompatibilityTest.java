package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.instance.efun.EngineEfuns;
import io.github.protasm.jvmud.engine.model.time.WorldScheduler;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import io.github.protasm.jvmud.language.execution.LPCObjectHandle;
import io.github.protasm.jvmud.language.execution.LPCRuntimeException;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Preserves callback identity, execution scopes and live-receiver selection as ownership moves. */
final class LPCScheduledCallbackCompatibilityTest {
    @TempDir
    Path directory;

    @Test
    void cancellationSelectsTheEarliestDueRegistrationThenItsIdWithoutReplacingDuplicates() {
        WorldScheduler scheduler = new WorldScheduler();
        MudlibRuntimeContext context = context(scheduler);
        CallbackReceiver receiver = register(context, "receiver");
        List<String> delivered = new ArrayList<>();
        receiver.onReceive = (label, payload) -> delivered.add(label);
        assertEquals(-1, context.cancelDeferredCallback("receive"));
        context.runWithCurrentObject(receiver, () -> {
            context.scheduleDeferredCallback("receive", 5, "registered first");
            context.scheduleDeferredCallback("receive", 2, "earliest old");
            context.scheduleDeferredCallback("receive", 2, "earliest new");
            context.scheduleDeferredCallback("receive", 4, "middle");
            assertEquals(-1, context.cancelDeferredCallback(null));
            assertEquals(-1, context.cancelDeferredCallback("absent"));
            assertEquals(0, context.cancelDeferredCallback("receive"));
        });

        scheduler.advanceTo(2);
        assertEquals(List.of("earliest new"), delivered, "Equal due times cancel in registration order");
        context.runWithCurrentObject(receiver, () -> assertEquals(0, context.cancelDeferredCallback("receive")));
        scheduler.advanceTo(5);

        assertEquals(List.of("earliest new", "registered first"), delivered);
        context.runWithCurrentObject(receiver, () -> assertEquals(-1, context.cancelDeferredCallback("receive")));
    }

    @Test
    void deferredCallsCopyTheArgumentArrayCaptureTheActorAndRestoreTheAmbientExecutionScope() {
        WorldScheduler scheduler = new WorldScheduler();
        MudlibRuntimeContext context = context(scheduler);
        CallbackReceiver receiver = register(context, "receiver");
        Object schedulingActor = new Object();
        Object ambientActor = new Object();
        Object ambientObject = new Object();
        RuntimeContext ambientRuntime = new MudlibRuntimeContext(null);
        List<Object> sharedValue = new ArrayList<>(List.of("before scheduling"));
        Object[] arguments = {"copied label", sharedValue};
        List<String> delivered = new ArrayList<>();
        receiver.onReceive = (label, payload) -> {
            assertSame(context, RuntimeContextHolder.current());
            assertSame(receiver, context.currentObject());
            assertSame(ambientObject, context.previousObject());
            assertSame(schedulingActor, context.currentCommandActor());
            assertSame(sharedValue, payload, "LPC argument values retain their object identities");
            assertEquals(List.of("before scheduling", "after scheduling"), payload);
            delivered.add(label);
        };
        context.withCommandActor(schedulingActor, () -> context.withCurrentObject(receiver, () -> {
            context.scheduleDeferredCallback("receive", 1, arguments);
            return null;
        }));
        arguments[0] = "changed caller array";
        sharedValue.add("after scheduling");

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(ambientRuntime);
        try {
            context.withCurrentObject(ambientObject, () -> context.withCommandActor(ambientActor, () -> {
                scheduler.advanceTo(1);
                assertSame(ambientRuntime, RuntimeContextHolder.current());
                assertSame(ambientObject, context.currentObject());
                assertSame(ambientActor, context.currentCommandActor());
                return null;
            }));
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }

        assertEquals(List.of("copied label"), delivered);
        assertNull(context.currentObject());
        assertNull(context.currentCommandActor());
    }

    @Test
    void actorlessDeferredCallsAndRecurringTicksInheritTheDeliveryActorRatherThanTheSchedulingActor() {
        WorldScheduler scheduler = new WorldScheduler();
        MudlibRuntimeContext context = context(scheduler);
        CallbackReceiver receiver = register(context, "receiver");
        Object schedulingActor = new Object();
        Object deliveryActor = new Object();
        Object ambientObject = new Object();
        RuntimeContext ambientRuntime = new MudlibRuntimeContext(null);
        List<String> delivered = new ArrayList<>();
        Runnable verifyDeliveryScope = () -> {
            assertSame(context, RuntimeContextHolder.current());
            assertSame(receiver, context.currentObject());
            assertSame(ambientObject, context.previousObject());
            assertSame(deliveryActor, context.currentCommandActor());
        };
        receiver.onReceive = (label, payload) -> {
            verifyDeliveryScope.run();
            delivered.add(label);
        };
        receiver.onTick = () -> {
            verifyDeliveryScope.run();
            delivered.add("recurring");
        };
        context.runWithCurrentObject(receiver, () -> context.scheduleDeferredCallback("receive", 1, "actorless deferred"));
        context.withCommandActor(schedulingActor, () -> context.withCurrentObject(receiver, () -> {
            context.scheduleRecurringTick(1, 1);
            return null;
        }));

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(ambientRuntime);
        try {
            context.withCurrentObject(ambientObject, () -> context.withCommandActor(deliveryActor, () -> {
                scheduler.advanceTo(1);
                assertSame(ambientRuntime, RuntimeContextHolder.current());
                assertSame(ambientObject, context.currentObject());
                assertSame(deliveryActor, context.currentCommandActor());
                return null;
            }));
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }

        assertEquals(List.of("actorless deferred", "recurring"), delivered);
        assertNull(context.currentObject());
        assertNull(context.currentCommandActor());
    }

    @Test
    void replacingTheSchedulerAffectsFutureRegistrationsWhileExistingHandlesKeepTheirOriginalClock() {
        WorldScheduler originalClock = new WorldScheduler();
        WorldScheduler replacementClock = new WorldScheduler();
        MudlibRuntimeContext context = context(originalClock);
        CallbackReceiver receiver = register(context, "receiver");
        List<String> delivered = new ArrayList<>();
        List<Long> recurringTicks = new ArrayList<>();
        receiver.onReceive = (label, payload) -> delivered.add(label);
        receiver.onTick = () -> recurringTicks.add(originalClock.currentTick());
        context.runWithCurrentObject(receiver, () -> {
            context.scheduleDeferredCallback("receive", 3, "old earliest");
            context.scheduleDeferredCallback("receive", 5, "old tied");
            context.scheduleDeferredCallback("receive", 8, "old surviving");
            context.scheduleRecurringTick(1, 2);
        });
        context.setScheduler(replacementClock);
        context.runWithCurrentObject(receiver, () -> {
            context.scheduleDeferredCallback("receive", 1, "new first");
            context.scheduleDeferredCallback("receive", 5, "new tied");
        });

        replacementClock.advanceTo(1);
        assertEquals(List.of("new first"), delivered);
        assertEquals(0, originalClock.currentTick());
        assertEquals(List.of(), recurringTicks);
        // Delivery on the new clock must not remove an old registration through a reused callback id.
        context.runWithCurrentObject(receiver, () -> assertEquals(0, context.cancelDeferredCallback("receive")));
        originalClock.advanceTo(3);
        assertEquals(List.of("new first"), delivered);
        // Equal absolute due ticks still choose the older registration, regardless of its clock.
        context.runWithCurrentObject(receiver, () -> assertEquals(0, context.cancelDeferredCallback("receive")));
        originalClock.advanceTo(5);
        replacementClock.advanceTo(5);
        assertEquals(List.of("new first", "new tied"), delivered);
        assertEquals(List.of(2L, 4L), recurringTicks);
        originalClock.advanceTo(8);

        assertEquals(List.of("new first", "new tied", "old surviving"), delivered);
        assertEquals(List.of(2L, 4L, 6L, 8L), recurringTicks);
        context.runWithCurrentObject(receiver, () -> {
            assertEquals(-1, context.cancelDeferredCallback("receive"));
            context.scheduleRecurringTick(0, 1);
        });
        originalClock.advanceTo(10);
        assertEquals(List.of(2L, 4L, 6L, 8L), recurringTicks, "Disable cancels the original clock's handle");
    }

    @Test
    void recurringReplacementDisableAndZeroIntervalKeepOneRegistrationPerObject() {
        WorldScheduler scheduler = new WorldScheduler();
        MudlibRuntimeContext context = context(scheduler);
        CallbackReceiver receiver = register(context, "receiver");
        List<Long> ticks = new ArrayList<>();
        receiver.onTick = () -> ticks.add(scheduler.currentTick());
        context.runWithCurrentObject(receiver, () -> context.scheduleRecurringTick(1, 4));
        scheduler.advanceTo(2);
        context.runWithCurrentObject(receiver, () -> context.scheduleRecurringTick(1, 2));
        scheduler.advanceTo(4);
        assertEquals(List.of(4L), ticks, "Replacing a tick cancels the previous task even at the same due time");

        context.runWithCurrentObject(receiver, () -> context.scheduleRecurringTick(1, 0));
        scheduler.advanceTo(6);
        assertEquals(List.of(4L, 5L, 6L), ticks, "A zero interval means one world tick");
        context.runWithCurrentObject(receiver, () -> context.scheduleRecurringTick(0, 99));
        scheduler.advanceTo(10);
        assertEquals(List.of(4L, 5L, 6L), ticks);
    }

    @Test
    void destructionCancelsBothKindsOfWorkWithoutAffectingAnotherObject() {
        WorldScheduler scheduler = new WorldScheduler();
        MudlibRuntimeContext context = context(scheduler);
        CallbackReceiver destroyed = register(context, "destroyed");
        CallbackReceiver surviving = register(context, "surviving");
        List<String> delivered = new ArrayList<>();
        destroyed.onReceive = (label, payload) -> {
            throw new AssertionError("Destroyed callback target must not run");
        };
        destroyed.onTick = () -> {
            throw new AssertionError("Destroyed recurring target must not run");
        };
        surviving.onReceive = (label, payload) -> delivered.add(label);
        surviving.onTick = () -> delivered.add("tick:" + scheduler.currentTick());
        for (CallbackReceiver target : List.of(destroyed, surviving)) {
            context.runWithCurrentObject(target, () -> {
                context.scheduleDeferredCallback("receive", 1, "deferred");
                context.scheduleRecurringTick(1, 2);
            });
        }

        context.destructObject(destroyed);
        scheduler.advanceTo(4);

        assertEquals(List.of("deferred", "tick:2", "tick:4"), delivered);
        context.runWithCurrentObject(destroyed, () -> assertEquals(-1, context.cancelDeferredCallback("receive")));
    }

    @Test
    void recurringFailureDisablesItsTickWhileADeferredFailureLeavesSiblingRegistrationsIntact() {
        WorldScheduler scheduler = new WorldScheduler();
        MudlibRuntimeContext context = context(scheduler);
        CallbackReceiver receiver = register(context, "receiver");
        List<Long> attemptedTicks = new ArrayList<>();
        List<String> delivered = new ArrayList<>();
        List<ReportedFailure> failures = new ArrayList<>();
        receiver.onTick = () -> {
            attemptedTicks.add(scheduler.currentTick());
            throw new IllegalStateException("tick failed");
        };
        receiver.onReceive = (label, payload) -> {
            if (label.equals("bad")) throw new IllegalStateException("callback failed");
            delivered.add(label);
        };
        context.setTimedRuntimeErrorHandler((target, kind, operation, error) -> {
            assertSame(context, RuntimeContextHolder.current());
            assertNull(context.currentObject(), "The failed invocation has unwound before error reporting");
            failures.add(new ReportedFailure(target, kind, operation, error));
        });
        context.runWithCurrentObject(receiver, () -> {
            context.scheduleRecurringTick(1, 1);
            context.scheduleDeferredCallback("receive", 1, "bad");
            context.scheduleDeferredCallback("receive", 2, "good");
        });
        RuntimeContext previous = RuntimeContextHolder.current();

        scheduler.advanceTo(4);

        assertEquals(List.of(1L), attemptedTicks);
        assertEquals(List.of("good"), delivered);
        assertEquals(List.of("scheduled_tick:tick", "deferred_callback:receive"),
                failures.stream().map(failure -> failure.kind() + ":" + failure.operation()).toList());
        for (ReportedFailure failure : failures) {
            assertSame(receiver, failure.target());
            assertInstanceOf(IllegalArgumentException.class, failure.error());
        }
        assertEquals("Failed to call generated LPC method: tick failed", failures.getFirst().error().getMessage());
        assertEquals("Failed to call generated LPC method: callback failed", failures.getLast().error().getMessage());
        assertSame(previous, RuntimeContextHolder.current());
        assertNull(context.currentObject());
        assertNull(context.currentCommandActor());
    }

    @Test
    void aFailingErrorReporterStillRestoresScopesAndLeavesOtherScheduledWorkDeliverable() {
        WorldScheduler scheduler = new WorldScheduler();
        MudlibRuntimeContext context = context(scheduler);
        CallbackReceiver receiver = register(context, "receiver");
        RuntimeContext ambientRuntime = new MudlibRuntimeContext(null);
        Object ambientObject = new Object();
        Object ambientActor = new Object();
        List<String> delivered = new ArrayList<>();
        receiver.onTick = () -> {
            throw new IllegalStateException("tick failed");
        };
        receiver.onReceive = (label, payload) -> delivered.add(label);
        context.setTimedRuntimeErrorHandler((target, kind, operation, error) -> {
            assertSame(context, RuntimeContextHolder.current());
            assertSame(ambientObject, context.currentObject());
            assertSame(ambientActor, context.currentCommandActor());
            throw new IllegalStateException("reporter failed");
        });
        context.runWithCurrentObject(receiver, () -> {
            context.scheduleRecurringTick(1, 1);
            context.scheduleDeferredCallback("receive", 2, "still pending");
        });

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(ambientRuntime);
        try {
            context.withCurrentObject(ambientObject, () -> context.withCommandActor(ambientActor, () -> {
                IllegalStateException error = assertThrows(IllegalStateException.class, () -> scheduler.advanceTo(1));
                assertEquals("reporter failed", error.getMessage());
                assertSame(ambientRuntime, RuntimeContextHolder.current());
                assertSame(ambientObject, context.currentObject());
                assertSame(ambientActor, context.currentCommandActor());
                scheduler.advanceTo(3);
                return null;
            }));
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }

        assertEquals(List.of("still pending"), delivered);
        assertNull(context.currentObject());
        assertNull(context.currentCommandActor());
    }

    @Test
    void pendingCallbacksAndRecurringTicksFollowReloadAndSurviveARejectedReplacement() throws Exception {
        String original = """
                int value;
                void start() {
                    jvmud_schedule_recurring_tick(1, 2);
                    jvmud_schedule_deferred_callback("finish", 3, 7);
                }
                void tick() { value += 1; }
                void finish(int amount) { value += amount; }
                int current() { return value; }
                """;
        Path source = directory.resolve("timer.c");
        Files.writeString(source, original);
        WorldScheduler scheduler = new WorldScheduler();
        MudlibRuntime runtime = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(directory).build());
        EngineEfuns.registerCore(runtime);
        runtime.setScheduler(scheduler);
        runtime.configureMudlib(MudlibConfiguration.builder().temporalTickMethod("tick").build());
        LPCObjectHandle timer = runtime.load("timer");
        timer.invoke("start");
        String replacement = original.replace("value += 1;", "value += 10;")
                .replace("value += amount;", "value += amount * 10;");
        Files.writeString(source, replacement);

        assertSame(timer.instance(), runtime.reload("timer").instance());
        scheduler.advanceTo(2);
        assertEquals(10, timer.invoke("current"));
        Files.writeString(source, "int incompatible_extra_field;\n" + replacement);
        assertThrows(LPCRuntimeException.class, () -> runtime.reload("timer"));
        scheduler.advanceTo(3);
        assertEquals(80, timer.invoke("current"), "The pending method name resolves the working replacement");
        scheduler.advanceTo(4);
        assertEquals(90, timer.invoke("current"), "A rejected replacement must not disturb recurring registration");
    }

    private static MudlibRuntimeContext context(WorldScheduler scheduler) {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        context.setScheduler(scheduler);
        context.configureMudlib(MudlibConfiguration.builder().temporalTickMethod("tick").build());
        // Standalone defaults ignore timed failures; an unexpected one must fail these scenarios.
        context.setTimedRuntimeErrorHandler((target, kind, operation, error) -> {
            throw new AssertionError("Unexpected " + kind + " failure in " + operation, error);
        });
        return context;
    }

    private static CallbackReceiver register(MudlibRuntimeContext context, String name) {
        CallbackReceiver receiver = new CallbackReceiver();
        context.registerObject(name, receiver);
        return receiver;
    }

    private record ReportedFailure(Object target, String kind, String operation, Throwable error) {}

    /** Public methods expose authored callback shapes without requiring source fixtures for each policy. */
    public static final class CallbackReceiver {
        private BiConsumer<String, Object> onReceive = (label, payload) -> {};
        private Runnable onTick = () -> {};

        public void receive(String label, Object payload) {
            onReceive.accept(label, payload);
        }

        public void tick() {
            onTick.run();
        }
    }
}
