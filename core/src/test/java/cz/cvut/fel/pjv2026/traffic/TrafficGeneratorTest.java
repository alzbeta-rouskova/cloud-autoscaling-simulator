package cz.cvut.fel.pjv2026.traffic;

import cz.cvut.fel.pjv2026.core.SimulationClock;
import cz.cvut.fel.pjv2026.model.ConstantServiceTimeModel;
import cz.cvut.fel.pjv2026.model.Request;
import cz.cvut.fel.pjv2026.model.RequestIdGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TrafficGeneratorTest {

    @Test
    void generates_correct_number_of_requests() {
        TrafficGenerator gen = new TrafficGenerator(
                new ConstantTrafficProfile(7),
                new RequestIdGenerator(),
                new ConstantServiceTimeModel(10L),
                new SimulationClock(100)
        );

        List<Request> requests = gen.generate(1L);

        assertEquals(7, requests.size());
    }

    @Test
    void all_requests_have_arrival_time_set() {
        SimulationClock clock = new SimulationClock(100);
        clock.advance();
        TrafficGenerator gen = new TrafficGenerator(
                new ConstantTrafficProfile(3),
                new RequestIdGenerator(),
                new ConstantServiceTimeModel(10L),
                clock
        );

        List<Request> requests = gen.generate(clock.tick());

        assertNotEquals(0L, requests.get(0).getArrivalTime());
    }
}
