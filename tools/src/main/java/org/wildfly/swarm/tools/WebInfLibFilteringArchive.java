package org.wildfly.swarm.tools;

import java.util.HashSet;
import java.util.Set;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.GenericArchiveImpl;

/**
 * @author Bob McWhirter
 */
public class WebInfLibFilteringArchive extends GenericArchiveImpl {

    public WebInfLibFilteringArchive(Archive<?> archive) {
        super( archive );
        filter();
    }

    protected void filter() {
        Set<ArchivePath> remove = new HashSet<>();
        filter( remove, getArchive().get(ArchivePaths.root() ) );

        for (ArchivePath each : remove) {
            getArchive().delete( each );
        }
    }

    protected void filter(Set<ArchivePath> remove, Node node) {
        String path = node.getPath().get();
        if ( path.startsWith( "/WEB-INF/lib" ) && path.endsWith( ".jar" ) ) {
            Asset asset = node.getAsset();
            if ( asset != null ) {
                Archive archive = ShrinkWrap.create(JavaArchive.class, path);
                archive.as(ZipImporter.class).importFrom( asset.openStream() );
                Node bootstrapNode = archive.get("/wildfly-swarm-bootstrap.conf");
                if ( bootstrapNode != null ) {
                    remove.add( node.getPath() );
                }
            }
        }

        for (Node each : node.getChildren()) {
            filter( remove, each );
        }
    }
}
