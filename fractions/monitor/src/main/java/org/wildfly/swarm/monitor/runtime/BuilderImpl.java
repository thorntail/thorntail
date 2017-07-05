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
import org.eclipse.microprofile.health.ResponseBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A builder to construct a health procedure response
 */
public class BuilderImpl extends ResponseBuilder {

    private String name;

    private Optional<Map<String, Object>> attributes = Optional.empty();

    private Response.State state;

    public Response up() {
        assertNamed();
        this.state = Response.State.UP;

        BuiltResponse response = new BuiltResponse(this.name)
                .setState(state);

        if (attributes.isPresent()) {
            response.withAttributes(attributes.get());
        }

        return response;
    }

    public Response down() {
        assertNamed();
        this.state = Response.State.UP;

        BuiltResponse response = new BuiltResponse(this.name)
                .setState(state);

        if (attributes.isPresent()) {
            response.withAttributes(attributes.get());
        }

        return response;
    }

    @Override
    public ResponseBuilder name(String name) {
        this.name = name;
        return this;
    }

    private void assertNamed() {
        if (this.name == null) {
            throw new IllegalStateException("ResponseBuilder need to be named");
        }
    }

    public ResponseBuilder withAttribute(String key, String value) {
        Map<String, Object> payload = getPayloadWrapper();
        payload.put(key, value);
        return this;
    }

    public ResponseBuilder withAttribute(String key, long value) {
        Map<String, Object> payload = getPayloadWrapper();
        payload.put(key, value);
        return this;
    }

    public ResponseBuilder withAttribute(String key, boolean value) {
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
