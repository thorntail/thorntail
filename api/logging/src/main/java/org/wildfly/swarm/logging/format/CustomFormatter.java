package org.wildfly.swarm.logging.format;

/**
 * @author Ken Finnigan
 */
public class CustomFormatter extends Formatter {
    private final String module;

    private final String className;

    public CustomFormatter(String name, String module, String className) {
        super(name, FormatterType.CUSTOM);
        this.module = module;
        this.className = className;
    }

    public String getModule() {
        return this.module;
    }

    public String getClassName() {
        return this.className;
    }
}
