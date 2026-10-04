package cz.arouskova.autoscaler.instance;

import cz.arouskova.autoscaler.model.Request;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bounded thread-safe queue of pending requests for a single service instance.
 * Wraps {@link ArrayBlockingQueue} and tracks a monotonic count of rejected
 * requests (drops caused by a full queue).
 * <p>
 * Marking the rejected request itself as {@code DROPPED} and any logging are
 * the responsibility of the caller (typically {@code ServiceInstance.submit}).
 * This class only owns queue mechanics and the drop counter.
 */
public class RequestQueue {

    private final ArrayBlockingQueue<Request> queue;
    private final AtomicInteger droppedCount = new AtomicInteger(0);

    /**
     * Creates an empty queue with the given fixed capacity.
     *
     * @param capacity maximum number of requests the queue can hold; must be positive
     */
    public RequestQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Attempts to enqueue the given request without blocking.
     * If the queue is full, increments the drop counter and returns {@code false};
     * the caller is responsible for marking the request as dropped.
     *
     * @param request to enqueue
     * @return {@code true} if the request was accepted, {@code false} if the queue was full
     */
    public boolean offer(Request request) {
        if (queue.offer(request)) {
            return true;
        }
        droppedCount.incrementAndGet();
        return false;
    }

    /**
     * Retrieves and removes the head of the queue, waiting up to the given
     * timeout if necessary. Used by worker threads as a blocking consumer.
     *
     * @param timeoutMs maximum time to wait in milliseconds, non-negative
     * @return the next request, or {@code null} if the timeout elapsed and the queue was empty
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public Request poll(long timeoutMs) throws InterruptedException {
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the current number of requests waiting in the queue.
     *
     * @return current queue size (0 when empty)
     */
    public int size() {
        return queue.size();
    }

    /**
     * Returns the total number of requests that have been rejected because the
     * queue was full. The counter is monotonically increasing — never resets.
     *
     * @return cumulative drop count since this queue was created
     */
    public int droppedCount() {
        return droppedCount.get();
    }
}
