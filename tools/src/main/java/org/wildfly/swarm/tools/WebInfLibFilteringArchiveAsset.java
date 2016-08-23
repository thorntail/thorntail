package org.wildfly.swarm.tools;

import java.io.InputStream;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * @author Bob McWhirter
 */
public class WebInfLibFilteringArchiveAsset implements ProjectAsset {

    private final ProjectAsset asset;

    private final DependencyManager dependencyManager;

    public WebInfLibFilteringArchiveAsset(ProjectAsset asset, DependencyManager dependencyManager) {
        this.asset = asset;
        this.dependencyManager = dependencyManager;
    }

    @Override
    public String getSimpleName() {
        return this.asset.getSimpleName();
    }

    @Override
    public Archive<?> getArchive() {
        return this.asset.getArchive();
    }

    @Override
    public InputStream openStream() {
        return new WebInfLibFilteringArchive( this.asset.getArchive(), this.dependencyManager ).as(ZipExporter.class).exportAsInputStream();
    }
}
