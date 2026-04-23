package cz.cvut.fel.pjv2026.model;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestIdGeneratorTest {

    @Test
    void generates_sequential_ids() {
        RequestIdGenerator gen = new RequestIdGenerator();

        for (long expected = 1; expected <= 1000; expected++) {
            assertEquals(expected, gen.nextId());
        }
    }

    @Test
    void is_thread_safe() throws InterruptedException {
        RequestIdGenerator gen = new RequestIdGenerator();
        int threadCount = 8;
        int idsPerThread = 1000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < idsPerThread; i++) {
                    ids.add(gen.nextId());
                }
                return null;
            });
        }

        start.countDown();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(threadCount * idsPerThread, ids.size());
    }
}
