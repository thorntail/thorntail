package io.thorntail.health;

import java.util.Map;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.State;
import org.junit.Assert;
import org.junit.Test;

public class HealthCheckResponseProviderTest {

    @Test
    public void testProvider() {

        HealthCheckResponse healthStatus = HealthCheckResponse.named("test")
                .up()
                .withData("a", "b")
                .withData("c", "d")
                .build();

        Assert.assertEquals("test", healthStatus.getName());
        Assert.assertSame(State.UP, healthStatus.getState());
        Map<String, Object> data = healthStatus.getData().get();
        Assert.assertEquals(2, data.size());
        Assert.assertEquals("Expected a", "b", data.get("a"));
        Assert.assertEquals("Expected c", "d", data.get("c"));
    }

}
