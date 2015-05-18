package org.wildfly.swarm;

import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class Swarm {

    public static void main(String...args) throws Exception {
        new Container().start();//.deploy();
    }
}
