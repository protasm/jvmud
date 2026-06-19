package io.github.protasm.jvmud.engine;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Advances deterministic world time from wall-clock time for a hosted world. */
public final class WorldClock implements AutoCloseable {
    private final Duration tickInterval;
    private final Runnable tick;
    private ScheduledExecutorService executor;

    public WorldClock(WorldRuntime worldRuntime, Duration tickInterval) {
        this(() -> worldRuntime.scheduler().advanceBy(1), tickInterval);
    }

    public WorldClock(Runnable tick, Duration tickInterval) {
        this.tick = Objects.requireNonNull(tick, "tick");
        this.tickInterval = Objects.requireNonNull(tickInterval, "tickInterval");
        if (tickInterval.isNegative() || tickInterval.isZero()) {
            throw new IllegalArgumentException("tickInterval must be greater than zero.");
        }
    }

    public synchronized void start() {
        if (executor != null) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "jvmud-world-clock");
            thread.setDaemon(true);
            return thread;
        });
        long periodNanos = tickInterval.toNanos();
        executor.scheduleAtFixedRate(this::runTick, periodNanos, periodNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public synchronized void close() {
        if (executor == null) {
            return;
        }
        executor.shutdownNow();
        executor = null;
    }

    private void runTick() {
        try {
            tick.run();
        } catch (RuntimeException e) {
            System.err.println("World clock tick failed: " + e.getMessage());
        }
    }
}
