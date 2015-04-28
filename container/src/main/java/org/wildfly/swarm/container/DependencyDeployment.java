package org.wildfly.swarm.container;

import org.jboss.modules.ArtifactLoaderFactory;
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
        this.file = ArtifactLoaderFactory.INSTANCE.getFile(gav);
        this.name = new File(ArtifactLoaderFactory.INSTANCE.gavToPath(gav)).getName();
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
