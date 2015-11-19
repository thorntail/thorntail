package org.wildfly.swarm.examples.messaging;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.config.messaging_activemq.Server;
import org.wildfly.swarm.config.messaging_activemq.server.*;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.msc.ServiceActivatorArchive;

import java.util.Arrays;

/**
 * @author Bob McWhirter
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Container container = new Container();

        container.fraction(MessagingFraction.createDefaultFraction()
                .defaultServer((s) -> {
                    s.jmsTopic("my-topic");
                    s.jmsQueue("my-queue");
                }));

        // Start the container
        container.start();

        JAXRSArchive appDeployment = ShrinkWrap.create(JAXRSArchive.class);
        appDeployment.addResource(MyResource.class);

        // Deploy your app
        container.deploy(appDeployment);

        JARArchive deployment = ShrinkWrap.create(JARArchive.class);
        deployment.addClass(MyService.class);
        deployment.as(ServiceActivatorArchive.class).addServiceActivator(MyServiceActivator.class);

        // Deploy the services
        container.deploy(deployment);

    }
}
