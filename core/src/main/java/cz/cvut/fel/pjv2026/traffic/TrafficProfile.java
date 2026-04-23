package cz.cvut.fel.pjv2026.traffic;

/**
 * Strategy determining how many requests should be generated in a given tick.
 * Implementations model different traffic patterns (constant, bursty, ...).
 */
public interface TrafficProfile {

    /**
     * Returns the number of requests that should arrive during the given tick.
     *
     * @param tick simulation tick index (non-negative)
     * @return number of requests for this tick; never negative
     */
    int requestsForTick(long tick);
}
