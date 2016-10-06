package org.wildfly.swarm.keycloak;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class SecuredTest {

    @Test
    public void testExistingWebXml() {
        WARArchive archive = ShrinkWrap.create( WARArchive.class );

        ClassLoaderAsset asset = new ClassLoaderAsset("test-web.xml");
        archive.addAsWebInfResource( asset, "web.xml" );

        archive.as(Secured.class)
                .protect( "/cheddar" );

        Node webXml = archive.get("WEB-INF/web.xml");

        Asset newAsset = webXml.getAsset();

        InputStream in = newAsset.openStream();
        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

        List<String> lines = reader.lines().map(String::trim).collect(Collectors.toList());

        assertThat( lines ).contains( "<servlet-name>comingsoon</servlet-name>" );
        assertThat( lines ).contains( "<url-pattern>/cheddar</url-pattern>" );
    }
}
