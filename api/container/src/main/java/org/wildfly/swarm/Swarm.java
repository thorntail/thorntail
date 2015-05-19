package org.wildfly.swarm;

import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class Swarm {

    public static void main(String...args) throws Exception {
        System.err.println( "----" + System.getProperty( "user.dir" ) );
        new Container().start().deploy();
    }
}
