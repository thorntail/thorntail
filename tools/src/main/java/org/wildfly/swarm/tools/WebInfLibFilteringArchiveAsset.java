package org.wildfly.swarm.tools;

import java.io.InputStream;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * @author Bob McWhirter
 */
public class WebInfLibFilteringArchiveAsset implements ProjectAsset {

    private final ProjectAsset asset;

    private final ResolvedDependencies resolvedDependencies;

    public WebInfLibFilteringArchiveAsset(ProjectAsset asset, ResolvedDependencies resolvedDependencies) {
        this.asset = asset;
        this.resolvedDependencies = resolvedDependencies;
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
        return new WebInfLibFilteringArchive( this.asset.getArchive(), this.resolvedDependencies ).as(ZipExporter.class).exportAsInputStream();
    }
}
