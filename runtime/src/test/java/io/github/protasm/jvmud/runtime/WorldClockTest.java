package io.github.protasm.jvmud.runtime;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

final class WorldClockTest {
    @Test
    void worldClockAdvancesWorldSchedulerFromWallClockTime() throws Exception {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test"));
        CountDownLatch secondTick = new CountDownLatch(2);
        runtime.scheduler().scheduleRecurring(1, 1, secondTick::countDown);

        try (WorldClock clock = new WorldClock(runtime, Duration.ofMillis(10))) {
            clock.start();

            assertTrue(secondTick.await(1, TimeUnit.SECONDS));
            assertTrue(runtime.scheduler().currentTick() >= 2);
        }
    }
}
