package cz.arouskova.autoscaler.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstantServiceTimeModelTest {

    @Test
    void returns_configured_service_time() {
        ConstantServiceTimeModel model = new ConstantServiceTimeModel(20L);

        assertEquals(20L, model.serviceTimeMs());
    }

    @Test
    void returns_same_value_on_repeated_calls() {
        ConstantServiceTimeModel model = new ConstantServiceTimeModel(15L);

        model.serviceTimeMs();
        model.serviceTimeMs();

        assertEquals(15L, model.serviceTimeMs());
    }
}
