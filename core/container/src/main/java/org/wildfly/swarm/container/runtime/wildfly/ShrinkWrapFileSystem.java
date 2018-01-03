package org.wildfly.swarm.container.runtime.wildfly;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSigner;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.vfs.TempDir;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.spi.FileSystem;

/**
 * Created by bob on 1/3/18.
 */
@ApplicationScoped
public class ShrinkWrapFileSystem implements FileSystem {

    public ShrinkWrapFileSystem() {

    }

    @PostConstruct
    public void postConstruct() throws IOException {
        this.tempDir = this.tempFileProvider.createTempDir("wildfly-swarm-deployments.tmp");
    }

    public void addArchive(String name, Archive<?> archive) {
        this.archives.put(name, new Entry(archive));
    }

    @Override
    public File getFile(VirtualFile mountPoint, VirtualFile target) throws IOException {
        return getEntry(mountPoint, target)
                .map(e -> getFile(e))
                .orElse(null);
    }

    File getFile(Entry entry) {
        if (entry.file == null) {
            try {
                entry.file = this.tempDir.createFile(entry.archive.getName(), entry.archive.as(ZipExporter.class).exportAsInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return entry.file;
    }

    @Override
    public InputStream openInputStream(VirtualFile mountPoint, VirtualFile target) throws IOException {
        return getEntry(mountPoint, target)
                .map(e -> e.archive.as(ZipExporter.class).exportAsInputStream()).orElse(null);
    }

    Optional<Entry> getEntry(VirtualFile mountPoint, VirtualFile target) {
        String name = target.getPathNameRelativeTo(mountPoint);
        Entry entry = this.archives.get(name);
        return Optional.ofNullable(entry);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean delete(VirtualFile mountPoint, VirtualFile target) {
        getEntry(mountPoint, target)
                .ifPresent(e -> {
                    try {
                        e.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
        return true;
    }

    @Override
    public long getSize(VirtualFile mountPoint, VirtualFile target) {
        return getEntry(mountPoint, target)
                .map(e -> sizeof(e))
                .orElse(0L);
    }

    long sizeof(Entry entry) {
        if (entry.size == null) {
            SizingOutputStream sizingOutputStream = new SizingOutputStream();
            entry.archive.as(ZipExporter.class).exportTo(sizingOutputStream);
            entry.size = sizingOutputStream.getSize();
        }
        return entry.size;
    }

    @Override
    public long getLastModified(VirtualFile mountPoint, VirtualFile target) {
        return 0;
    }

    @Override
    public boolean exists(VirtualFile mountPoint, VirtualFile target) {
        return getEntry(mountPoint, target)
                .isPresent();
    }

    @Override
    public boolean isFile(VirtualFile mountPoint, VirtualFile target) {
        return exists(mountPoint, target);
    }

    @Override
    public boolean isDirectory(VirtualFile mountPoint, VirtualFile target) {
        return false;
    }

    @Override
    public List<String> getDirectoryEntries(VirtualFile mountPoint, VirtualFile target) {
        return Collections.emptyList();
    }

    @Override
    public CodeSigner[] getCodeSigners(VirtualFile mountPoint, VirtualFile target) {
        return new CodeSigner[0];
    }

    @Override
    public void close() throws IOException {
        for (Entry each : this.archives.values()) {
            each.close();
        }
        this.tempDir.close();
    }

    @Override
    public File getMountSource() {
        return null;
    }

    @Override
    public URI getRootURI() throws URISyntaxException {
        return null;
    }

    private Map<String, Entry> archives = new HashMap<>();

    private TempDir tempDir;

    @Inject
    private TempFileProvider tempFileProvider;

    private static class Entry implements Closeable {
        Entry(Archive<?> archive) {
            this.archive = archive;
        }

        @Override
        public void close() throws IOException {
            if (this.file != null) {
                this.file.delete();
            }
        }

        final Archive<?> archive;

        Long size;

        File file;
    }


}
