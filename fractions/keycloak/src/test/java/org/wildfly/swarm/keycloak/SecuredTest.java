package org.wildfly.swarm.keycloak;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

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

    @Test
    public void testKeycloakJsonFromClasspath() throws Exception {
        WARArchive archive = ShrinkWrap.create(WARArchive.class);
        archive.as(Secured.class)
                .protect("/cheddar");

        String keycloakJson = extractKeycloakJson(archive);
        assertThat(keycloakJson).contains("http://localhost:9090/auth");
    }

    @Test
    public void testOverriddenByExternalKeycloakJson() throws Exception {
        System.setProperty("swarm.keycloak.json.path", "src/test/resources/external-keycloak.json");

        WARArchive archive = ShrinkWrap.create(WARArchive.class);
        archive.as(Secured.class)
                .protect("/cheddar");

        String keycloakJson = extractKeycloakJson(archive);
        assertThat(keycloakJson).contains("http://localhost:19090/auth");

        System.clearProperty("swarm.keycloak.json.path");
    }

    private static String extractKeycloakJson(Archive archive) throws Exception {
        String keycloakPath = "WEB-INF/keycloak.json";
        Node keycloakJson = archive.get(keycloakPath);
        if (keycloakJson == null) {
            fail();
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(keycloakJson.getAsset().openStream()))) {
            reader.lines().forEach(sb::append);
            return sb.toString();
        }
    }

}
