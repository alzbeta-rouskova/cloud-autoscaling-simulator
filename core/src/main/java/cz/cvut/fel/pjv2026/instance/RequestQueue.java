package cz.cvut.fel.pjv2026.instance;

import cz.cvut.fel.pjv2026.model.Request;

import java.util.concurrent.ArrayBlockingQueue;

public class RequestQueue {

    private final ArrayBlockingQueue<Request> queue;

    public RequestQueue(int capacity) {

        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public boolean offer(Request r) {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public int size() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public int droppedCount() {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
