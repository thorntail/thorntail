package io.thorntail.plugins.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * Created by bob on 2/13/18.
 */
class FileEntry extends AbstractEntry {

    public FileEntry(Path path, File content) {
        super(path);
        this.content = content;
    }

    public File getContent() {
        return this.content;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(this.content);
    }

    @Override
    public FileTime getLastModifiedTime() {
        try {
            return Files.getLastModifiedTime(this.content.toPath());
        } catch (IOException e) {
            return super.getLastModifiedTime();
        }
    }

    private final File content;
}
