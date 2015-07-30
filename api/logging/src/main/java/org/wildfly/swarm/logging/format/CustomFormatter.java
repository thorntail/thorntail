package org.wildfly.swarm.logging.format;

import java.util.Properties;

/**
 * @author Ken Finnigan
 */
public class CustomFormatter extends Formatter {
    private final String module;

    private final String className;

    private final Properties properties;

    public CustomFormatter(String name, String module, String className) {
        this(name, module, className, new Properties());
    }

    public CustomFormatter(String name, String module, String className, Properties properties) {
        super(name, FormatterType.CUSTOM);
        this.module = module;
        this.className = className;
        this.properties = properties;
    }

    public String getModule() {
        return this.module;
    }

    public String getClassName() {
        return this.className;
    }

    public Properties properties() {
        return this.properties;
    }
}
