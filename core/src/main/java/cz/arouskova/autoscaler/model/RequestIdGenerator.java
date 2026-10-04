package cz.arouskova.autoscaler.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread-safe generator for unique request IDs.
 * This class uses an AtomicLong to ensure that each generated ID is unique even when accessed concurrently by multiple threads.
 */
public class RequestIdGenerator {

    private final AtomicLong counter = new AtomicLong(0);

    /**
     * Generates the next unique request ID.
     *
     * @return the next unique request ID as a long value
     */
    public long nextId() {
        return counter.incrementAndGet();
    }

}
