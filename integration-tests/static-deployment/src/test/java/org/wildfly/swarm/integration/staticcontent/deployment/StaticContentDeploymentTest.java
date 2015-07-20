package org.wildfly.swarm.integration.staticcontent.deployment;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class StaticContentDeploymentTest extends AbstractWildFlySwarmTestCase {

    @Test
    public void testStaticContent() throws Exception {
        Container container = newContainer();
        container.start();
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.staticContent();
        container.deploy( deployment );
        assertThat( fetch( "http://localhost:8080/static-content.txt" ) ).contains( "This is static." );
        assertThat( fetch( "http://localhost:8080/index.html" ) ).contains( "This is index.html." );
        assertThat( fetch( "http://localhost:8080/foo/index.html" ) ).contains( "This is foo/index.html." );
        assertThat( fetch( "http://localhost:8080/" ) ).contains( "This is index.html." );
        assertThat( fetch( "http://localhost:8080/foo" ) ).contains( "This is foo/index.html." );
        container.stop();
    }

    @Test
    public void testStaticContentWithContext() throws Exception {
        Container container = newContainer();
        container.start();
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.setContextRoot( "/static" );
        deployment.staticContent();
        container.deploy( deployment );
        assertThat( fetch( "http://localhost:8080/static/static-content.txt" ) ).contains( "This is static." );
        assertThat( fetch( "http://localhost:8080/static/index.html" ) ).contains( "This is index.html." );
        assertThat( fetch( "http://localhost:8080/static/foo/index.html" ) ).contains( "This is foo/index.html." );
        assertThat( fetch( "http://localhost:8080/static" ) ).contains( "This is index.html." );
        assertThat( fetch( "http://localhost:8080/static/foo" ) ).contains( "This is foo/index.html." );
        container.stop();
    }
}
