package org.wildfly.swarm.asciidoctorj.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

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
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
        deployment.addClass(MyResource.class);
        deployment.setContextRoot("rest");
        deployment.addAllDependencies();
        swarm.deploy(deployment);
    }

    public static void stopMain() throws Exception {
        swarm.stop();
    }
}
