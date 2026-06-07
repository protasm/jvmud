package io.github.protasm.jvmud.runtime;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Owns deterministic world time and scheduled work for one JVMud world.
 *
 * <p>The scheduler does not run on a background thread. Callers explicitly advance world time, which
 * makes tests, admin tooling, and future persistence checkpoints repeatable.</p>
 */
public final class WorldScheduler {
    private final PriorityQueue<ScheduledWork> queue = new PriorityQueue<>(
            Comparator.comparingLong(ScheduledWork::dueTick)
                    .thenComparingLong(ScheduledWork::order));
    private long currentTick;
    private long nextTaskId = 1;
    private long nextOrder = 1;

    /** Returns the current deterministic world tick. */
    public long currentTick() {
        return currentTick;
    }

    /**
     * Schedules one action after a non-negative number of ticks.
     *
     * @return a handle that can cancel the scheduled action before it runs
     */
    public ScheduledTask scheduleAfter(long delayTicks, Runnable action) {
        requireNonNegative(delayTicks, "delayTicks");
        Objects.requireNonNull(action, "action");
        ScheduledTask task = new ScheduledTask(this, nextTaskId++);
        enqueue(task, currentTick + delayTicks, 0, action);
        return task;
    }

    /**
     * Schedules a recurring action.
     *
     * @param initialDelayTicks non-negative delay before the first run
     * @param intervalTicks positive interval between later runs
     * @return a handle that can cancel future occurrences
     */
    public ScheduledTask scheduleRecurring(long initialDelayTicks, long intervalTicks, Runnable action) {
        requireNonNegative(initialDelayTicks, "initialDelayTicks");
        if (intervalTicks <= 0) {
            throw new IllegalArgumentException("intervalTicks must be greater than zero.");
        }
        Objects.requireNonNull(action, "action");
        ScheduledTask task = new ScheduledTask(this, nextTaskId++);
        enqueue(task, currentTick + initialDelayTicks, intervalTicks, action);
        return task;
    }

    /** Advances world time by the supplied non-negative number of ticks. */
    public void advanceBy(long ticks) {
        requireNonNegative(ticks, "ticks");
        advanceTo(currentTick + ticks);
    }

    /**
     * Advances world time to an absolute target tick and runs all due work in deterministic order.
     *
     * @throws IllegalArgumentException if the target would move time backward
     */
    public void advanceTo(long targetTick) {
        if (targetTick < currentTick) {
            throw new IllegalArgumentException("targetTick cannot move world time backward.");
        }

        while (!queue.isEmpty() && queue.peek().dueTick() <= targetTick) {
            ScheduledWork work = queue.poll();
            if (work.task().cancelled()) {
                continue;
            }
            currentTick = work.dueTick();
            work.action().run();
            if (work.recurring() && !work.task().cancelled()) {
                enqueue(work.task(), currentTick + work.intervalTicks(), work.intervalTicks(), work.action());
            }
        }

        currentTick = targetTick;
    }

    boolean cancel(ScheduledTask task) {
        Objects.requireNonNull(task, "task");
        if (task.cancelled()) {
            return false;
        }
        task.markCancelled();
        return true;
    }

    private void enqueue(ScheduledTask task, long dueTick, long intervalTicks, Runnable action) {
        queue.add(new ScheduledWork(task, dueTick, intervalTicks, action, nextOrder++));
    }

    private void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be non-negative.");
        }
    }

    private record ScheduledWork(
            ScheduledTask task,
            long dueTick,
            long intervalTicks,
            Runnable action,
            long order) {
        boolean recurring() {
            return intervalTicks > 0;
        }
    }
}
