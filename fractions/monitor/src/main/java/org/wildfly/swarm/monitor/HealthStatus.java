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

    private final String name;

    private Optional<ModelNode> message = Optional.empty();
    private State state;

    HealthStatus(String name) {
        this.name = name;
    }

    public static HealthStatus named(String name)
    {
        return new HealthStatus(name);
    }

    public HealthStatus up() {
        assertNamed();
        this.state = State.UP;
        return this;
    }

    private void assertNamed() {
        if(null==this.name)
            throw new IllegalStateException("HealthStatus need to be named");
    }

    public HealthStatus down() {
        this.state = State.DOWN;
        return this;
    }

    public HealthStatus withAttribute(String key, String value) {
        ModelNode payload = getPayloadWrapper();
        payload.get(key).set(value);
        return this;
    }

    public HealthStatus withAttribute(String key, long value) {
        ModelNode payload = getPayloadWrapper();
        payload.get(key).set(value);
        return this;
    }

    public HealthStatus withAttribute(String key, boolean b) {
        ModelNode payload = getPayloadWrapper();
        payload.get(key).set(b);
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

    @Override
    public String toJson() {
        ModelNode wrapper = new ModelNode();
        wrapper.get("id").set(name);
        wrapper.get("result").set(state.name());
        if(message.isPresent()) {
            wrapper.get("data").set(message.get());
        }
        return wrapper.toJSONString(false);
    }
}
