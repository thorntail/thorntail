package org.wildfly.swarm.logging;

/**
 * @author Bob McWhirter
 */
public class FileHandler {

    private final String name;
    private final String path;
    private final String level;
    private final String formatter;

    public FileHandler(String name, String path, String level, String formatter) {
        this.name = name;
        this.path = path;
        this.level = level;
        this.formatter = formatter;
    }

    public String name() {
        return this.name;
    }

    public String path() {
        return this.path;
    }

    public String level() {
        return this.level;
    }

    public String formatter() {
        return this.formatter;
    }
}
