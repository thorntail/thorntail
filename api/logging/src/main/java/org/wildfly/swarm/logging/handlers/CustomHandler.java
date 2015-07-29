package org.wildfly.swarm.logging.handlers;

import java.util.Properties;

/**
 * @author Ken Finnigan
 */
public class CustomHandler extends Handler {

    private final String module;

    private final String className;

    private final Properties properties;

    public CustomHandler(String name, String module, String className, Properties properties, String formatter) {
        super(name, null, formatter, HandlerType.CUSTOM);
        this.module = module;
        this.className = className;
        this.properties = properties;
    }

    public String module() {
        return this.module;
    }

    public String className() {
        return this.className;
    }

    public Properties properties() {
        return this.properties;
    }
}