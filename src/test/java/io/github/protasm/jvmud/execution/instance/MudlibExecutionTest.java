package io.github.protasm.jvmud.execution.instance;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MudlibExecutionTest {
    @Test
    void inputAndTicksShareOneQueueAndNeverExecuteTogether() throws Exception {
        CountDownLatch inputStarted = new CountDownLatch(1);
        CountDownLatch releaseInput = new CountDownLatch(1);
        CountDownLatch tickAfterInput = new CountDownLatch(1);
        AtomicReference<Thread> inputThread = new AtomicReference<>();
        AtomicReference<Thread> tickThread = new AtomicReference<>();
        try (MudlibExecution execution = new MudlibExecution("queue-test", Duration.ofMillis(10), () -> {
            if (inputStarted.getCount() == 0) {
                tickThread.set(Thread.currentThread());
                tickAfterInput.countDown();
            }
        })) {
            var first = CompletableFuture.runAsync(() -> execution.call(() -> {
                inputThread.set(Thread.currentThread());
                inputStarted.countDown();
                assertTrue(releaseInput.await(3, TimeUnit.SECONDS));
                return null;
            }));
            try {
                assertTrue(inputStarted.await(2, TimeUnit.SECONDS));
                var second = CompletableFuture.supplyAsync(() -> execution.call(Thread::currentThread));
                assertFalse(tickAfterInput.await(100, TimeUnit.MILLISECONDS));
                assertFalse(second.isDone());
                releaseInput.countDown();
                first.get(2, TimeUnit.SECONDS);
                assertSame(inputThread.get(), second.get(2, TimeUnit.SECONDS));
                assertTrue(tickAfterInput.await(2, TimeUnit.SECONDS));
                assertSame(inputThread.get(), tickThread.get());
            } finally { releaseInput.countDown(); }
        }
    }
}
