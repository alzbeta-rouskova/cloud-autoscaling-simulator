package cz.cvut.fel.pjv2026.traffic;

import cz.cvut.fel.pjv2026.core.SimulationClock;
import cz.cvut.fel.pjv2026.model.Request;
import cz.cvut.fel.pjv2026.model.RequestIdGenerator;
import cz.cvut.fel.pjv2026.model.ServiceTimeModel;

import java.util.List;

public class TrafficGenerator {

    private final TrafficProfile profile;
    private final RequestIdGenerator idGenerator;
    private final ServiceTimeModel serviceTimeModel;
    private final SimulationClock clock;

    public TrafficGenerator(TrafficProfile profile, RequestIdGenerator idGenerator, ServiceTimeModel serviceTimeModel, SimulationClock clock) {
        this.profile = profile;
        this.idGenerator = idGenerator;
        this.serviceTimeModel = serviceTimeModel;
        this.clock = clock;
    }

    public List<Request> generate(int tick) {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
