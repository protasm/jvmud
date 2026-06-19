package io.github.protasm.jvmud.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.time.ScheduledTask;
import io.github.protasm.jvmud.engine.time.WorldScheduler;
import io.github.protasm.jvmud.engine.world.World;
import io.github.protasm.jvmud.engine.world.WorldRuntime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class WorldSchedulerTest {
    @Test
    void delayedWorkRunsWhenWorldTimeReachesItsTick() {
        WorldScheduler scheduler = new WorldScheduler();
        List<String> events = new ArrayList<>();

        scheduler.scheduleAfter(3, () -> events.add("arrived@" + scheduler.currentTick()));

        scheduler.advanceBy(2);
        assertTrue(events.isEmpty());

        scheduler.advanceBy(1);
        assertEquals(List.of("arrived@3"), events);
        assertEquals(3, scheduler.currentTick());
    }

    @Test
    void workRunsInDueTickThenRegistrationOrder() {
        WorldScheduler scheduler = new WorldScheduler();
        List<String> events = new ArrayList<>();

        scheduler.scheduleAfter(2, () -> events.add("second"));
        scheduler.scheduleAfter(1, () -> events.add("first"));
        scheduler.scheduleAfter(2, () -> events.add("third"));

        scheduler.advanceTo(2);

        assertEquals(List.of("first", "second", "third"), events);
    }

    @Test
    void cancelledWorkDoesNotRun() {
        WorldScheduler scheduler = new WorldScheduler();
        List<String> events = new ArrayList<>();

        ScheduledTask task = scheduler.scheduleAfter(1, () -> events.add("cancelled"));

        assertTrue(task.cancel());
        assertTrue(task.cancelled());
        assertFalse(task.cancel());

        scheduler.advanceTo(1);

        assertTrue(events.isEmpty());
    }

    @Test
    void recurringWorkRunsUntilCancelled() {
        WorldScheduler scheduler = new WorldScheduler();
        List<Long> ticks = new ArrayList<>();

        ScheduledTask task = scheduler.scheduleRecurring(1, 2, () -> ticks.add(scheduler.currentTick()));

        scheduler.advanceTo(5);
        assertEquals(List.of(1L, 3L, 5L), ticks);

        task.cancel();
        scheduler.advanceTo(9);

        assertEquals(List.of(1L, 3L, 5L), ticks);
    }

    @Test
    void recurringWorkCanCancelItself() {
        WorldScheduler scheduler = new WorldScheduler();
        List<Long> ticks = new ArrayList<>();
        ScheduledTask[] handle = new ScheduledTask[1];

        handle[0] = scheduler.scheduleRecurring(0, 1, () -> {
            ticks.add(scheduler.currentTick());
            handle[0].cancel();
        });

        scheduler.advanceTo(3);

        assertEquals(List.of(0L), ticks);
    }

    @Test
    void rejectsInvalidTimeRequests() {
        WorldScheduler scheduler = new WorldScheduler();

        assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleAfter(-1, () -> {}));
        assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleRecurring(0, 0, () -> {}));
        assertThrows(IllegalArgumentException.class, () -> scheduler.advanceBy(-1));

        scheduler.advanceTo(2);
        assertThrows(IllegalArgumentException.class, () -> scheduler.advanceTo(1));
    }

    @Test
    void worldRuntimeOwnsAWorldScheduler() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));

        runtime.scheduler().scheduleAfter(1, () -> {});
        runtime.scheduler().advanceBy(1);

        assertEquals(1, runtime.scheduler().currentTick());
    }
}
