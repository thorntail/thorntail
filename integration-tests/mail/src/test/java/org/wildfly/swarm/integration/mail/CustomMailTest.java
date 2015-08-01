package org.wildfly.swarm.integration.mail;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.mail.MailFraction;
import org.wildfly.swarm.mail.SmtpServer;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
public class CustomMailTest extends AbstractWildFlySwarmTestCase implements ContainerFactory {

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container()
                .fraction(new MailFraction()
                        .smtpServer(new SmtpServer("Default")
                                .host("localhost")
                                .port("25")));
    }

    @Deployment
    public static Archive createDeployment() {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.staticContent();
        return deployment;
    }

    @Test
    @RunAsClient
    public void testIt() throws IOException {
        String result = fetch("http://localhost:8080/static-content.txt");
        assertThat(result).contains("This is static.");
    }

}
