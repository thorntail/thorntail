package org.wildfly.swarm.shrinkwrap;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.wildfly.swarm.container.DefaultDeployment;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Bob McWhirter
 */
public class ShrinkWrapDeployment<T extends Archive> { //implements Deployment {

    final protected T archive;

    public ShrinkWrapDeployment(String name, Class<T> archiveType) {
        this.archive = ShrinkWrap.create(archiveType, name);
    }

    public T getArchive() {
        return this.archive;
    }

    public String getName() {
        return this.archive.getName();
    }

    public VirtualFile getContent() throws IOException {
        InputStream in = this.archive.as(ZipExporter.class).exportAsInputStream();
        VirtualFile mountPoint = VFS.getRootVirtualFile().getChild( this.archive.getName() );
        try {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            TempFileProvider tempFileProvider = TempFileProvider.create("wildfly-swarm", executor);
            VFS.mountZip(in, this.archive.getName(), mountPoint, tempFileProvider);

            DefaultDeployment.ensureJBossWebXml( mountPoint );

            return mountPoint;
        } finally {
            in.close();
        }
    }
}

