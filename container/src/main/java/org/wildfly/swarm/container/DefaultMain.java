package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public class DefaultMain {

    public static void main(String[] args) throws Exception {
        new Container().start().deploy();
    }
}
