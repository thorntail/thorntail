package org.wildfly.swarm.integration.staticcontent.war;

import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.undertow.WarDeployment;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class StaticContentWarTest extends AbstractWildFlySwarmTestCase {

    @Test
    public void testStaticContent() throws Exception {
        Container container = newContainer();
        container.start();

        WarDeployment deployment = new WarDeployment(container);
        deployment.staticContent();
        container.deploy(deployment);

        String result = fetch("http://localhost:8080/static-content.txt");
        assertThat(result).contains("This is static.");

        result = fetch("http://localhost:8080/foo/foo-content.txt");
        assertThat(result).contains("This is foo.");
        container.stop();
    }

    @Test
    public void testStaticContentWithBase() throws Exception {
        Container container = newContainer();
        container.start();

        WarDeployment deployment = new WarDeployment(container);
        deployment.staticContent("/", "foo");
        container.deploy(deployment);

        String result = fetch("http://localhost:8080/foo-content.txt");
        assertThat(result).contains("This is foo.");
        container.stop();
    }
}
