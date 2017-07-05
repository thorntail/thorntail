/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICES file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package org.wildfly.swarm.monitor.runtime;

import org.eclipse.microprofile.health.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A builder to construct a health procedure response
 */
class BuiltResponse extends Response {

    private String name;
    private Response.State state;
    private Optional<Map<String, Object>> attributes = Optional.empty();

    BuiltResponse(String name) {
        this.name = name;
    }

    BuiltResponse setName(String name) {
        this.name = name;
        return this;
    }

    BuiltResponse setState(Response.State state) {
        this.state = state;
        return this;
    }

    public String getName() {
        return name;
    }

    public Response.State getState() {
        return state;
    }

    public Optional<Map<String, Object>> getAttributes() {
        return attributes;
    }

    public BuiltResponse withAttribute(String key, String value) {
        getPayloadWrapper().put(key,value);
        return this;
    }

    public BuiltResponse withAttribute(String key, long value) {
        getPayloadWrapper().put(key,value);
        return this;
    }

    public BuiltResponse withAttribute(String key, boolean value) {
        getPayloadWrapper().put(key,value);
        return this;
    }

    private Map<String, Object> getPayloadWrapper() {
        if (!this.attributes.isPresent()) {
            this.attributes = Optional.of(new HashMap<>());
        }
        return this.attributes.get();
    }

    BuiltResponse withAttributes(Map<String, Object> attributes) {
        getPayloadWrapper().putAll(attributes);
        return this;
    }
}
