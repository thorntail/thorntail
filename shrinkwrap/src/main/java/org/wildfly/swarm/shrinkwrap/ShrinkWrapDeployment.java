package org.wildfly.swarm.shrinkwrap;

import org.jboss.as.server.deployment.module.TempFileProviderService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.wildfly.swarm.container.Deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Bob McWhirter
 */
public class ShrinkWrapDeployment implements Deployment {

    private final WebArchive archive;

    public ShrinkWrapDeployment(String name) {
        //this.archive = ShrinkWrap.create(JavaArchive.class, name);
        this.archive = ShrinkWrap.create(WebArchive.class, name);
    }

    public WebArchive getArchive() {
        return this.archive;
    }

    public String getName() {
        return this.archive.getName();
    }

    @Override
    public VirtualFile getContent() throws IOException {
        System.err.println("classload: " + Thread.currentThread().getContextClassLoader());
        //ZipExporter exporter = ShrinkWrap.create(ZipExporter.class);
        //File tmpFile = File.createTempFile(archive.getName(), archive.getId());
        //ZipExporter exporter = new ZipExporterImpl(this.archive);
        //exporter.exportTo(tmpFile, true);
        //System.err.println( "export to " + tmpFile );
        //return tmpFile;

        //this.archive.
        InputStream in = this.archive.as(ZipExporter.class).exportAsInputStream();
        VirtualFile mountPoint = VFS.getRootVirtualFile().getChild( this.archive.getName() );
        try {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            TempFileProvider tempFileProvider = TempFileProvider.create( "wildfly-swarm", executor );
            VFS.mountZip(in, this.archive.getName(), mountPoint, tempFileProvider );
            return mountPoint;
        } finally {
            in.close();
        }
    }
}

