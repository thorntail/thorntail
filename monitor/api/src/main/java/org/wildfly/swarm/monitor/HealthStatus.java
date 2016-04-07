/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.monitor;

import java.util.Optional;

import org.jboss.dmr.ModelNode;

/**
 * @author Heiko Braun
 * @since 23/03/16
 */
public class HealthStatus implements Status {

    private Optional<ModelNode> message = Optional.empty();
    private final State state;

    HealthStatus(State state) {
        this.state = state;
    }

    public static HealthStatus up() {
        return new HealthStatus(State.UP);
    }

    public static HealthStatus down() {
        return new HealthStatus(State.DOWN);
    }

    public HealthStatus withAttribute(String key, String value) {
        ModelNode payload = getPayloadWrapper();
        payload.set(key, value);
        return this;
    }

    public HealthStatus withAttribute(String key, long value) {
            ModelNode payload = getPayloadWrapper();
            payload.set(key, value);
            return this;
        }

    public HealthStatus withAttribute(String key, boolean b) {
        ModelNode payload = getPayloadWrapper();
        payload.set(key, b);
        return this;
    }

    private ModelNode getPayloadWrapper() {
        if(!this.message.isPresent())
            this.message = Optional.of(new ModelNode());
        return this.message.get();
    }

    public Optional<String> getMessage() {
        return message.isPresent() ? Optional.of(getPayloadWrapper().toJSONString(false)) : Optional.empty();
    }

    public State getState() {
        return state;
    }
}
