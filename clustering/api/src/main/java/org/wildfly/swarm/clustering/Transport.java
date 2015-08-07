package org.wildfly.swarm.clustering;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class Transport {

    private final String name;
    private final String socketBinding;
    private final Map<String,String> properties = new HashMap<>();

    public Transport(String name, String socketBinding) {
        this.name = name;
        this.socketBinding = socketBinding;
    }

    public String name() {
        return this.name;
    }

    public String socketBinding() {
        return this.socketBinding;
    }

    public Transport property(String name, String value) {
        this.properties.put( name, value);
        return this;
    }

    public Map<String,String> properties() {
        return this.properties;
    }
}
