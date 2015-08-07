package org.wildfly.swarm.clustering;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class Protocol {

    private final String name;
    private final Map<String,String> properties = new HashMap<>();

    public Protocol(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public Protocol property(String name, String value) {
        this.properties.put( name, value );
        return this;
    }

    public Map<String,String> properties() {
        return this.properties;
    }
}
