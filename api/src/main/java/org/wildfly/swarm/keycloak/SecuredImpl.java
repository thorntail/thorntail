package org.wildfly.swarm.keycloak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;
import org.wildfly.swarm.container.JARArchive;

/**
 * @author Bob McWhirter
 */
public class SecuredImpl extends AssignableBase<ArchiveBase<?>> implements Secured {

    private SecuredWebXmlAsset asset;

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public SecuredImpl(ArchiveBase<?> archive) {
        super(archive);

        Node node = getArchive().as(JARArchive.class).get("WEB-INF/web.xml");
        if ( node == null ) {
            this.asset = new SecuredWebXmlAsset();
            getArchive().as( JARArchive.class).add( this.asset );
        } else if ( ! ( node.getAsset() instanceof SecuredWebXmlAsset ) ) {
            throw new RuntimeException( "Secured may not be used when providing a custom WEB-INF/web.xml" );
        }

        getArchive().as(JARArchive.class).addModule( "org.wildfly.swarm.keycloak", "runtime" );
        getArchive().as(JARArchive.class).addAsServiceProvider("io.undertow.servlet.ServletExtension", "org.wildfly.swarm.runtime.keycloak.SecurityContextServletExtension" );

        InputStream keycloakJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");
        if ( keycloakJson == null ) {

            String appArtifact = System.getProperty("wildfly.swarm.app.artifact");

            if (appArtifact != null) {
                try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("_bootstrap/" + appArtifact)) {
                    ZipImporterImpl importer = new ZipImporterImpl(archive);
                    importer.importFrom(in);
                    Node jsonNode = archive.get("keycloak.json");
                    if ( jsonNode == null ) {
                        jsonNode = archive.get("WEB-INF/keycloak.json");
                    }

                    if ( jsonNode != null && jsonNode.getAsset() != null ) {
                        keycloakJson = jsonNode.getAsset().openStream();
                    }
                } catch (IOException e) {
                    // ignore
                    // e.printStackTrace();
                }
            }
        }

        if ( keycloakJson != null ) {
            getArchive().as( JARArchive.class ).add( createAsset( keycloakJson ), "WEB-INF/keycloak.json" );
        } else {
            // not adding it.
        }
    }

    @Override
    public SecurityConstraint protect() {
        return this.asset.protect();
    }

    @Override
    public SecurityConstraint protect(String urlPattern) {
        return this.asset.protect( urlPattern );
    }

    private Asset createAsset(InputStream in) {

        StringBuilder str = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader( new InputStreamReader( in ) ) ) {

            String line = null;

            while ( ( line = reader.readLine() ) != null ) {
                str.append( line ).append( "\n" );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayAsset( str.toString().getBytes() );
    }
}

