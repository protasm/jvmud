package io.github.protasm.jvmud.execution.instance;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** One mudlib's command queue and clock, sharing a single execution thread. */
final class MudlibExecution implements AutoCloseable {
    private final ScheduledThreadPoolExecutor executor;
    private volatile Thread worker;
    private ScheduledFuture<?> clock;

    /** Creates an independent queue; zero interval leaves time under explicit control. */
    MudlibExecution(String gameId, Duration interval, Runnable tick) {
        executor = new ScheduledThreadPoolExecutor(1, action -> {
            Thread thread = new Thread(action, "jvmud-mudlib-" + gameId);
            thread.setDaemon(true);
            worker = thread;
            return thread;
        });
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        if (!interval.isZero()) {
            long nanos = interval.toNanos();
            clock = executor.scheduleWithFixedDelay(() -> {
                try { tick.run(); }
                catch (RuntimeException | LinkageError e) {
                    System.err.println("Mudlib " + gameId + " tick failed: " + e.getMessage());
                }
            }, nanos, nanos, TimeUnit.NANOSECONDS);
        }
    }

    /** Queues external calls; nested calls already on the world thread run directly. */
    <T> T call(Callable<T> operation) {
        if (Thread.currentThread() == worker) return invoke(operation);
        try {
            return executor.submit(operation).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for mudlib execution.", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            if (e.getCause() instanceof Error error) throw error;
            throw new IllegalStateException("Mudlib execution failed.", e.getCause());
        }
    }

    /** Stops future ticks without interrupting a command or tick already running. */
    void stopClock() {
        if (clock != null) clock.cancel(false);
    }

    /** Closes the queue after its owner's shutdown lifecycle has completed. */
    @Override public void close() { executor.shutdown(); }

    private static <T> T invoke(Callable<T> operation) {
        try { return operation.call(); }
        catch (RuntimeException | Error e) { throw e; }
        catch (Exception e) { throw new IllegalStateException("Mudlib execution failed.", e); }
    }
}
