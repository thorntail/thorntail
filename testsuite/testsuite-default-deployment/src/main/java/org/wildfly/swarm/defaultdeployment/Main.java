package org.wildfly.swarm.defaultdeployment;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.Swarm;

/**
 * @author Bob McWhirter
 */
public class Main {

    private static Swarm swarm;

    private Main() {

    }

    public static void main(String... args) throws Exception {
        swarm = new Swarm(args);
        swarm.start();
        Archive<?> deployment = swarm.createDefaultDeployment();

        Node persistenceXml = deployment.get("WEB-INF/classes/META-INF/persistence.xml");

        if (persistenceXml == null) {
            throw new Error("persistence.xml is not found");
        }

        if (persistenceXml.getAsset() == null) {
            throw new Error("persistence.xml is not found");
        }

        swarm.deploy(deployment);
    }

    public static void stopMain() throws Exception {
        swarm.stop();
    }
}
