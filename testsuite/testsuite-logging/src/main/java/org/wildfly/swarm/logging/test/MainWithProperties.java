package org.wildfly.swarm.logging.test;

import org.wildfly.swarm.Swarm;

/**
 * @author Bob McWhirter
 */
public class MainWithProperties {

    private static Swarm swarm;

    private MainWithProperties() {
    }

    public static void main(String... args) throws Exception {
        System.setProperty("swarm.logging", "TRACE");
        System.setProperty("swarm.logging.custom.category", "DEBUG");
        System.setProperty("swarm.logging.pattern-formatters.MY_COLOR_PATTERN.pattern", "%K{level}%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p (%t) [%c.%M()] %s%e%n");
        swarm = new Swarm(args);
        swarm.start().deploy();
    }

    public static void stopMain() throws Exception {
        swarm.stop();
    }

}
