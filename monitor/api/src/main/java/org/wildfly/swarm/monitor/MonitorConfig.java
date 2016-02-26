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
