package org.wildfly.swarm.container;

import org.jboss.modules.ArtifactLoaderFactory;
import org.jboss.modules.ProjectDependencies;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class DependencyDeployment implements Deployment {

    private final File file;
    private final String name;

    protected static String name(String gav) {
        String[] parts = gav.split( ":" );
        if ( parts.length >= 2 ) {
            return parts[1] + ".jar";
        }

        return gav;
    }

    public DependencyDeployment(String gav) throws IOException {
        this( gav, name(gav) );
    }

    public DependencyDeployment(String gav, String name) throws IOException {
        String versionedGav = ProjectDependencies.getProjectDependencies().getVersionedGAV(gav);
        this.file = ArtifactLoaderFactory.INSTANCE.getFile(versionedGav);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public VirtualFile getContent() throws IOException {
        VirtualFile mountPoint = VFS.getRootVirtualFile().getChild(this.name);
        VFS.mountReal(this.file, mountPoint);
        return mountPoint;
    }
}
