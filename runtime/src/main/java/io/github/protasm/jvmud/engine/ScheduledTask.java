package io.github.protasm.jvmud.engine;

/** A cancellable handle for work registered with a {@link WorldScheduler}. */
public final class ScheduledTask {
    private final WorldScheduler scheduler;
    private final long id;
    private boolean cancelled;

    ScheduledTask(WorldScheduler scheduler, long id) {
        this.scheduler = scheduler;
        this.id = id;
    }

    public long id() {
        return id;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public boolean cancel() {
        return scheduler.cancel(this);
    }

    void markCancelled() {
        cancelled = true;
    }
}
