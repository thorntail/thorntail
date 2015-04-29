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

    public DependencyDeployment(String gav) throws IOException {
        String versionedGav = ProjectDependencies.getProjectDependencies().getVersionedGAV(gav);
        this.file = ArtifactLoaderFactory.INSTANCE.getFile(versionedGav);
        this.name = new File(ArtifactLoaderFactory.INSTANCE.gavToPath(versionedGav)).getName();
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
