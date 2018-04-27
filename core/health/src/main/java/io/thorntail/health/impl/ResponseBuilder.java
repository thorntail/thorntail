package io.thorntail.health.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Created by bob on 1/16/18.
 */
class ResponseBuilder extends HealthCheckResponseBuilder {

    @Override
    public HealthCheckResponseBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, String value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, long value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, boolean value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder up() {
        this.state = HealthCheckResponse.State.UP;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder down() {
        this.state = HealthCheckResponse.State.DOWN;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder state(boolean up) {
        if (up) {
            return up();
        }

        return down();
    }

    @Override
    public HealthCheckResponse build() {
        return new Response(this.name, this.state, this.data.isEmpty() ? null : this.data);
    }

    private String name;

    private HealthCheckResponse.State state = HealthCheckResponse.State.DOWN;

    private Map<String, Object> data = new HashMap<>();
}
