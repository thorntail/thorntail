/*
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * /
 */

package org.wildfly.swarm.microprofile.health;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A builder to construct a health procedure response
 */
public class BuilderImpl extends HealthCheckResponseBuilder {

    private String name;

    private Optional<Map<String, Object>> attributes = Optional.empty();

    private HealthCheckResponse.State state;

    public HealthCheckResponseBuilder up() {
        return state(true);
    }

    public HealthCheckResponseBuilder down() {
        return state(false);
    }

    @Override
    public HealthCheckResponse build() {
        assertNamed();

        BuiltResponse response = new BuiltResponse(this.name)
                .setState(state);

        if (attributes.isPresent()) {
            response.withAttributes(attributes.get());
        }

        return response;
    }

    @Override
    public HealthCheckResponseBuilder state(boolean up) {
        this.state = up ? HealthCheckResponse.State.UP : HealthCheckResponse.State.DOWN;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder name(String name) {
        this.name = name;
        return this;
    }

    private void assertNamed() {
        if (this.name == null) {
            throw new IllegalStateException("ResponseBuilder need to be named");
        }
    }

    public HealthCheckResponseBuilder withData(String key, String value) {
        Map<String, Object> payload = getPayloadWrapper();
        payload.put(key, value);
        return this;
    }

    public HealthCheckResponseBuilder withData(String key, long value) {
        Map<String, Object> payload = getPayloadWrapper();
        payload.put(key, value);
        return this;
    }

    public HealthCheckResponseBuilder withData(String key, boolean value) {
        Map<String, Object> payload = getPayloadWrapper();
        payload.put(key, value);
        return this;
    }

    private Map<String, Object> getPayloadWrapper() {
        if (!this.attributes.isPresent()) {
            this.attributes = Optional.of(new HashMap<>());
        }
        return this.attributes.get();
    }
}
