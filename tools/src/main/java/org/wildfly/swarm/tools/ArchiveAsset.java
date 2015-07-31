package org.wildfly.swarm.tools;

import java.io.InputStream;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * @author Bob McWhirter
 */
public class ArchiveAsset extends org.jboss.shrinkwrap.api.asset.ArchiveAsset implements ProjectAsset {

    public ArchiveAsset(Archive archive) {
        super(archive, ZipExporter.class);
    }

    @Override
    public String getSimpleName() {
        return getArchive().getName();
    }

}
