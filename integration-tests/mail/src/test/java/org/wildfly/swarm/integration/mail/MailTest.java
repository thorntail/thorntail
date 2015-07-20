package org.wildfly.swarm.integration.mail;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.mail.MailFraction;
import org.wildfly.swarm.mail.SmtpServer;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class MailTest extends AbstractWildFlySwarmTestCase {
    private Container container;

    @Test
    public void testSimple() throws Exception {
        container = newContainer();
        container.start();


        WARArchive deployment = ShrinkWrap.create( WARArchive.class );
        deployment.staticContent();
        container.deploy(deployment);

        String result = fetch("http://localhost:8080/static-content.txt");
        assertThat(result).contains("This is static.");
    }

    @Test
    public void testCustom() throws Exception {
        container = newContainer();

        container.fraction(new MailFraction()
                .smtpServer(new SmtpServer("Default")
                        .host("localhost")
                        .port("25")));
        container.start();


        WARArchive deployment = ShrinkWrap.create( WARArchive.class );
        deployment.staticContent();
        container.deploy(deployment);

        String result = fetch("http://localhost:8080/static-content.txt");
        assertThat(result).contains("This is static.");
    }

    @After
    public void shutdown() throws Exception {
        container.stop();
    }
}
