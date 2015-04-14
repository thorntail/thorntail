package org.wildfly.boot.shrinkwrap;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.wildfly.boot.container.Deployment;

import java.io.File;
import java.io.IOException;

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

    @Override
    public File getContent() throws IOException {
        System.err.println("classload: " + Thread.currentThread().getContextClassLoader());
        //ZipExporter exporter = ShrinkWrap.create(ZipExporter.class);
        File tmpFile = File.createTempFile(archive.getName(), archive.getId());
        ZipExporter exporter = new ZipExporterImpl(this.archive);
        exporter.exportTo(tmpFile, true);
        System.err.println( "export to " + tmpFile );
        return tmpFile;
    }
}

