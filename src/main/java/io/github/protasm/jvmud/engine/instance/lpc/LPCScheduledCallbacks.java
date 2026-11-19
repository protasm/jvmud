package io.github.protasm.jvmud.engine.instance.lpc;

import io.github.protasm.jvmud.engine.model.time.ScheduledTask;
import io.github.protasm.jvmud.engine.model.time.WorldScheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Owns recurring LPC ticks and deferred named-method callbacks for one mudlib.
 *
 * <p>{@link WorldScheduler} owns deterministic time and generic task ordering. This owner associates its
 * handles with stable LPC object identities, keeps duplicate method registrations independent,
 * and applies LPC cancellation and failure policy. Named callbacks select the object's current
 * behavior at delivery, so reloading a program does not replace its registrations.</p>
 *
 * <p>Deferred calls capture their scheduling actor and a shallow copy of their argument array.
 * Recurring ticks capture no actor. Delivery uses the existing invocation and command owners;
 * the host supplies the active runtime binding, registration checks, temporal-method policy and
 * failure reporting. A null captured actor leaves any ambient actor scope intact.</p>
 *
 * <p>All hosted operations run on the instance's execution queue. This owner introduces no timer
 * thread; standalone tests advance the scheduler explicitly. The existing public timing parameters
 * are passed through as scheduler ticks without converting wall-clock units.</p>
 */
final class LPCScheduledCallbacks {
    private final LPCObjectLifecycle objectLifecycle;
    private final LPCInvocationStack invocationStack;
    private final LPCMethodInvoker methodInvoker;
    private final LPCCommands commands;
    private final Host host;
    private WorldScheduler scheduler = new WorldScheduler();
    private final Map<Object, ScheduledTask> recurringTickTasks = new IdentityHashMap<>();
    private final Map<Object, Map<String, List<DeferredCallback>>> deferredCallbackTasks =
            new IdentityHashMap<>();
    private long nextDeferredCallbackId = 1;

    /** Shares existing invocation owners and the host's generated-code and lifecycle boundaries. */
    LPCScheduledCallbacks(LPCObjectLifecycle objectLifecycle, LPCInvocationStack invocationStack,
            LPCMethodInvoker methodInvoker, LPCCommands commands, Host host) {
        this.objectLifecycle = Objects.requireNonNull(objectLifecycle, "objectLifecycle");
        this.invocationStack = Objects.requireNonNull(invocationStack, "invocationStack");
        this.methodInvoker = Objects.requireNonNull(methodInvoker, "methodInvoker");
        this.commands = Objects.requireNonNull(commands, "commands");
        this.host = Objects.requireNonNull(host, "host");
    }

    /** Cancels both forms of scheduled work at the host's existing destruction boundary. */
    void discardObject(Object target) {
        cancelRecurringTick(target);
        cancelDeferredCallbacks(target);
    }

    /**
     * Selects the clock for future registrations, using a fresh isolated clock when null.
     * Existing task handles remain on their original clocks; registrations and IDs are retained.
     */
    void setScheduler(WorldScheduler scheduler) {
        this.scheduler = scheduler != null ? scheduler : new WorldScheduler();
    }

    /**
     * Schedules a one-shot callback on the current object without replacing other callbacks that
     * happen to target the same method.
     *
     * <p>Method names are not task identities: mudlibs commonly fan out an event by scheduling the
     * same delivery method once per subscriber. Each registration therefore remains independently
     * cancellable and deliverable. The active command actor is also captured per registration so
     * delayed work observes the same actor that initiated it. Tick units are explicit here;
     * validation messages retain the public API's historical parameter names.</p>
     */
    void scheduleDeferred(String methodName, int delayTicks, Object... args) {
        if (methodName == null || methodName.isBlank()) {
            return;
        }
        if (delayTicks < 0) {
            throw new IllegalArgumentException("delaySeconds cannot be negative.");
        }

        Object target = invocationStack.currentObject();
        if (target == null) {
            return;
        }

        Map<String, List<DeferredCallback>> tasks =
                deferredCallbackTasks.computeIfAbsent(target, ignored -> new LinkedHashMap<>());
        long callbackId = nextDeferredCallbackId++;
        long dueTick = scheduler.currentTick() + delayTicks;
        Object commandActor = commands.currentActor();
        Object[] invocationArgs = args == null ? new Object[0] : args.clone();
        ScheduledTask task = scheduler.scheduleAfter(
                delayTicks,
                () -> deliverDeferredCallback(
                        target, methodName, callbackId, commandActor, invocationArgs));
        tasks.computeIfAbsent(methodName, ignored -> new ArrayList<>())
                .add(new DeferredCallback(callbackId, dueTick, task));
    }

    /**
     * Cancels the current object's earliest matching registration by due tick, then registration ID.
     * Returns LPC zero when removed, or minus one when absent; the result is not a remaining delay.
     */
    int cancelDeferred(String methodName) {
        if (methodName == null) {
            return -1;
        }

        Object target = invocationStack.currentObject();
        if (target == null) {
            return -1;
        }

        Map<String, List<DeferredCallback>> tasks = deferredCallbackTasks.get(target);
        if (tasks == null) {
            return -1;
        }

        List<DeferredCallback> callbacks = tasks.get(methodName);
        if (callbacks == null || callbacks.isEmpty()) {
            return -1;
        }

        DeferredCallback callback = callbacks.stream()
                .min(Comparator.comparingLong(DeferredCallback::dueTick)
                        .thenComparingLong(DeferredCallback::id))
                .orElseThrow();
        callbacks.remove(callback);
        callback.task().cancel();
        if (callbacks.isEmpty()) {
            tasks.remove(methodName);
        }
        if (tasks.isEmpty()) {
            deferredCallbackTasks.remove(target);
        }
        return 0;
    }

    /**
     * Replaces the current object's recurring registration. Zero disables it; any nonzero value
     * enables it, and a zero interval uses one scheduler tick. No command actor is captured.
     */
    void scheduleRecurring(int enabled, int intervalTicks) {
        if (intervalTicks < 0) {
            throw new IllegalArgumentException("intervalSeconds cannot be negative.");
        }

        Object target = invocationStack.currentObject();
        if (target == null) {
            return;
        }

        cancelRecurringTick(target);
        if (enabled == 0) {
            return;
        }

        long recurringInterval = intervalTicks > 0 ? intervalTicks : 1;
        ScheduledTask task = scheduler.scheduleRecurring(
                recurringInterval,
                recurringInterval,
                () -> deliverRecurringTick(target));
        recurringTickTasks.put(target, task);
    }

    /** Removes the recurring registration before cancelling its scheduler handle. */
    private void cancelRecurringTick(Object target) {
        ScheduledTask task = recurringTickTasks.remove(target);
        if (task != null) {
            task.cancel();
        }
    }

    /** Discards all deferred registrations for a destroyed identity and cancels their handles. */
    private void cancelDeferredCallbacks(Object target) {
        Map<String, List<DeferredCallback>> tasks = deferredCallbackTasks.remove(target);
        if (tasks == null) {
            return;
        }

        for (List<DeferredCallback> callbacks : tasks.values()) {
            for (DeferredCallback callback : callbacks) {
                callback.task().cancel();
            }
        }
    }

    /**
     * Uses the current tick-method policy and receiver. Missing methods skip this occurrence;
     * invocation failures disable the registration before the host reports the error.
     */
    private void deliverRecurringTick(Object target) {
        if (!recurringTickTasks.containsKey(target)) {
            return;
        }

        String methodName = host.temporalTickMethod();
        if (methodName == null || !methodInvoker.hasMethod(
                objectLifecycle.receiverOf(target).getClass(), methodName, 0)) {
            return;
        }

        // Method preflight remains outside binding and invocation-error reporting.
        host.withRuntimeContext(() -> {
            try {
                methodInvoker.invokeRequired(target, methodName);
            } catch (RuntimeException | LinkageError e) {
                cancelRecurringTick(target);
                host.reportError(target, "scheduled_tick", methodName, e);
            }
        });
    }

    /** Consumes this registration before registry/method checks or invocation; failures are not retried. */
    private void deliverDeferredCallback(
            Object target,
            String methodName,
            long callbackId,
            Object commandActor,
            Object[] args) {
        Map<String, List<DeferredCallback>> tasks = deferredCallbackTasks.get(target);
        if (tasks != null) {
            List<DeferredCallback> callbacks = tasks.get(methodName);
            if (callbacks != null) {
                callbacks.removeIf(callback -> callback.id() == callbackId);
                if (callbacks.isEmpty()) {
                    tasks.remove(methodName);
                }
            }
            if (tasks.isEmpty()) {
                deferredCallbackTasks.remove(target);
            }
        }

        if (!host.isRegisteredTarget(target)
                || !methodInvoker.hasMethodAcceptingMissingArguments(
                        objectLifecycle.receiverOf(target).getClass(), methodName, args.length)) {
            return;
        }

        // A captured actor gets its own scope; an uncaptured actor leaves ambient state intact.
        host.withRuntimeContext(() -> {
            try {
                if (commandActor == null) {
                    methodInvoker.invokeRequired(target, methodName, args);
                } else {
                    commands.withActor(commandActor, () -> methodInvoker.invokeRequired(target, methodName, args));
                }
            } catch (RuntimeException | LinkageError e) {
                host.reportError(target, "deferred_callback", methodName, e);
            }
        });
    }

    /** Independent registration identity and deadline, retaining its original scheduler handle. */
    private record DeferredCallback(long id, long dueTick, ScheduledTask task) {}

    /** Hosted policy and execution services used without owning the full mudlib context. */
    interface Host {
        /** Reads the currently configured exact-zero-argument recurring tick method, or null. */
        String temporalTickMethod();

        /** Checks the host's current canonical object registry before deferred delivery. */
        boolean isRegisteredTarget(Object target);

        /** Binds invocation and reporting, restoring the previous runtime in finally even if either throws. */
        void withRuntimeContext(Runnable action);

        /** Reports a timed failure; a reporter exception propagates through the enclosing binding. */
        void reportError(Object target, String context, String operation, Throwable error);
    }
}
