package org.wildfly.swarm;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class Swarm {

    public static void main(String... args) throws Exception {
        Container container = new Container().start();
        container.deploy();
    }
}
