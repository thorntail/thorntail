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
import java.util.Set;

/**
 * @author Heiko Braun
 */
public class MonitorConfig {

    private final HashMap<Key, Object> config = new HashMap<>();

    public MonitorConfig() {

    }

    public MonitorConfig put(Key key, Object value) {
        config.put(key, value);
        return this;
    }

    public Object get(Key key) {
        return config.get(key);
    }

    public Set<Map.Entry<Key, Object>> entrySet() {
        return config.entrySet();
    }

    public enum Key {
        WEB_CONTEXT
    }
}
