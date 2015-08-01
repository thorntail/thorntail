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
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.mail.MailFraction;
import org.wildfly.swarm.mail.SmtpServer;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
public class SimpleMailTest extends AbstractWildFlySwarmTestCase {

    @Deployment
    public static Archive createDeployment() {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.staticContent();
        return deployment;
    }

    @Test
    @RunAsClient
    public void testSimple() throws IOException {
        String result = fetch("http://localhost:8080/static-content.txt");
        assertThat(result).contains("This is static.");
    }

}
