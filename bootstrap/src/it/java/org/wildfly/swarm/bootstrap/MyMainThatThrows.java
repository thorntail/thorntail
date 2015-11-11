package org.wildfly.swarm.bootstrap;

/**
 * @author Bob McWhirter
 */
public class MyMainThatThrows {

    public static void main(String...args) throws Throwable {
        throw new Exception("expected to throw");

    }
}
