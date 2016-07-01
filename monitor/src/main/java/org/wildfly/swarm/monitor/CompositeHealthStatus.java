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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jboss.dmr.ModelNode;

/**
 * @author Heiko Braun
 * @since 23/03/16
 */
public class CompositeHealthStatus implements Status {

    private final Policy policy;

    private final List<HealthStatus> states;

    private final static Policy DEFAULT_POLICY = new Policy() {
        @Override
        public State apply(List<HealthStatus> states) {
            boolean isDown = false;

            for (HealthStatus check : states) {
                if(State.DOWN == check.getState()) {
                    isDown = true;
                    break;
                }
            }
            return isDown ? State.DOWN : State.UP;
        }

        @Override
        public Optional<String> message(List<HealthStatus> states) {
            ModelNode payload = new ModelNode();

            for (HealthStatus state : states) {
                if(state.getMessage().isPresent())
                    payload.set(state.getMessage().get());
            }
            return payload.isDefined() ? Optional.of(payload.toJSONString(false)) : Optional.empty();
        }
    };

    CompositeHealthStatus(Policy policy) {
        this.policy = policy;
        this.states = new ArrayList<>();
    }

    CompositeHealthStatus() {
        this(DEFAULT_POLICY);
    }

    CompositeHealthStatus(List<HealthStatus> checks) {
        this();
        this.states.addAll(checks);
    }

    @Override
    public State getState() {
        return this.policy.apply(this.states);
    }

    @Override
    public Optional<String> getMessage() {
        return this.policy.message(this.states);
    }

    public static CompositeHealthStatus build() {
        return new CompositeHealthStatus();
    }

    public static CompositeHealthStatus build(Policy policy) {
        return new CompositeHealthStatus(policy);
    }

    public static CompositeHealthStatus buildFrom(List<HealthStatus> checks) {
        return new CompositeHealthStatus(checks);
    }

    public static CompositeHealthStatus buildFrom(HealthStatus... checks) {
        List<HealthStatus> list = new ArrayList<>(checks.length);
        for (HealthStatus check : checks) {
            list.add(check);
        }
        return new CompositeHealthStatus(list);
    }


    public interface Policy {
        State apply(List<HealthStatus> states);
        Optional<String> message(List<HealthStatus> states);
    }
}
