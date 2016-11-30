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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Heiko Braun
 * @since 23/03/16
 */
public class HealthStatus implements Status {

    private static final String ID = "id";

    private static final String RESULT = "result";

    private static final String DATA = "data";

    private final String name;

    private Optional<Map<String, Object>> message = Optional.empty();
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
        Map<String,Object> payload = getPayloadWrapper();
        payload.put(key, value);
        return this;
    }

    public HealthStatus withAttribute(String key, long value) {
        Map<String,Object> payload = getPayloadWrapper();
        payload.put(key, value);
        return this;
    }

    public HealthStatus withAttribute(String key, boolean value) {
        Map<String,Object> payload = getPayloadWrapper();
        payload.put(key, value);
        return this;
    }

    private Map<String,Object> getPayloadWrapper() {
        if(!this.message.isPresent())
            this.message = Optional.of(new HashMap<>());
        return this.message.get();
    }

    public Optional<String> getMessage() {
        return message.isPresent() ? Optional.of(toJson()) : Optional.empty();
    }

    public State getState() {
        return state;
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"").append(ID).append("\":\"").append(name).append("\",");
        sb.append("\"").append(RESULT).append("\":\"").append(state.name()).append("\",");
        if(message.isPresent()) {
            sb.append("\"").append(DATA).append("\": {");
            Map<String, Object> atts = message.get();
            int i = 0;
            for(String key : atts.keySet()) {
                sb.append("\"").append(key).append("\":").append(encode(atts.get(key)));
                if(i<atts.keySet().size()-1)
                    sb.append(",");
                i++;
            }
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    private String encode(Object o) {
        String res = null;
        if(o instanceof String) {
            res = "\""+o.toString()+"\"";
        }
        else {
            res = o.toString();
        }

        return res;
    }
}
