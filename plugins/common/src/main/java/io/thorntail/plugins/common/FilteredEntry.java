package io.thorntail.plugins.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Created by bob on 2/13/18.
 */
public class FilteredEntry implements Entry {

    public FilteredEntry(Entry delegate, String key, String value) {
        this.delegate = delegate;
        this.key = key;
        this.value = value;
    }

    @Override
    public Path getPath() {
        return this.delegate.getPath();
    }

    @Override
    public InputStream openStream() throws IOException {
        StringBuilder str = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.delegate.openStream()))) {
            reader.lines()
                    .map(line -> line.replace("#" + this.key + "#", value))
                    .forEach(line -> {
                        str.append(line);
                        str.append("\n");
                    });
        }
        return new ByteArrayInputStream(str.toString().getBytes());
    }

    @Override
    public Entry filter(String key, String value) {
        return new FilteredEntry(this, key, value);
    }

    @Override
    public Entry withPermissions(PosixFilePermission... permissions) {
        return new EntryWithPermissions( this, permissions );
    }

    @Override
    public Set<PosixFilePermission> getPermissions() {
        return this.delegate.getPermissions();
    }

    @Override
    public FileTime getLastModifiedTime() {
        return this.delegate.getLastModifiedTime();
    }

    private final Entry delegate;

    private final String key;

    private final String value;
}
