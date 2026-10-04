package cz.arouskova.autoscaler.traffic;

import cz.arouskova.autoscaler.core.SimulationClock;
import cz.arouskova.autoscaler.model.Request;
import cz.arouskova.autoscaler.model.RequestIdGenerator;
import cz.arouskova.autoscaler.model.ServiceTimeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Produces requests for a given simulation tick according to a {@link TrafficProfile}.
 * Each generated {@link Request} is stamped with the current simulated time from
 * {@link SimulationClock} and a service time from {@link ServiceTimeModel}.
 */
public class TrafficGenerator {

    private static final Logger log = LoggerFactory.getLogger(TrafficGenerator.class);

    private final TrafficProfile profile;
    private final RequestIdGenerator idGenerator;
    private final ServiceTimeModel serviceTimeModel;
    private final SimulationClock clock;

    /**
     * Creates a traffic generator.
     *
     * @param profile          profile deciding how many requests arrive per tick
     * @param idGenerator      source of unique request ids
     * @param serviceTimeModel model providing service time for each request
     * @param clock            simulation clock used as arrival time source
     */
    public TrafficGenerator(TrafficProfile profile,
                            RequestIdGenerator idGenerator,
                            ServiceTimeModel serviceTimeModel,
                            SimulationClock clock) {
        this.profile = profile;
        this.idGenerator = idGenerator;
        this.serviceTimeModel = serviceTimeModel;
        this.clock = clock;
    }

    /**
     * Generates a list of requests for the given tick. The count is determined
     * by {@link TrafficProfile#requestsForTick(long)}.
     *
     * @param tick simulation tick index
     * @return list of newly created requests (may be empty, never null)
     */
    public List<Request> generate(long tick) {
        int count = profile.requestsForTick(tick);
        List<Request> requests = new ArrayList<>(count);
        long arrivalTime = clock.simulatedTimeMs();
        for (int i = 0; i < count; i++) {
            requests.add(new Request(
                    idGenerator.nextId(),
                    arrivalTime,
                    serviceTimeModel.serviceTimeMs()
            ));
        }
        log.debug("Generated {} request(s) for tick {}", count, tick);
        return requests;
    }
}
