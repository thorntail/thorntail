package io.thorntail.health.impl;

import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * Created by bob on 1/16/18.
 */
class Response extends HealthCheckResponse {

    Response(String name, State state, Map<String, Object> data) {
        this.name = name;
        this.state = state;
        this.data = data;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public Optional<Map<String, Object>> getData() {
        return Optional.ofNullable(this.data);
    }

    private final String name;

    private final State state;

    private final Map<String, Object> data;
}
