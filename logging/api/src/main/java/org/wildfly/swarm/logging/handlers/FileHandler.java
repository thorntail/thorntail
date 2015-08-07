package org.wildfly.swarm.logging.handlers;

/**
 * @author Bob McWhirter
 */
public class FileHandler extends Handler {

    private final String path;

    public FileHandler(String name, String path, String level, String formatter) {
        super(name, level, formatter, HandlerType.FILE);
        this.path = path;
    }

    public String path() {
        return this.path;
    }
}
