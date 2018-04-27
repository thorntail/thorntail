package io.thorntail.plugins.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bob on 2/13/18.
 */
public class EntryWithPermissions implements Entry {

    public EntryWithPermissions(Entry delegate, PosixFilePermission...permissions) {
        this.delegate = delegate;
        for (PosixFilePermission permission : permissions) {
            this.permissions.add( permission );
        }
    }

    @Override
    public Path getPath() {
        return this.delegate.getPath();
    }

    @Override
    public InputStream openStream() throws IOException {
        return this.delegate.openStream();
    }

    @Override
    public Entry filter(String key, String value) {
        return new FilteredEntry( this, key, value );
    }

    @Override
    public Entry withPermissions(PosixFilePermission... permissions) {
        return new EntryWithPermissions(this, permissions);
    }

    @Override
    public Set<PosixFilePermission> getPermissions() {
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.addAll( this.delegate.getPermissions() );
        permissions.addAll( this.permissions );
        return permissions;
    }

    @Override
    public FileTime getLastModifiedTime() {
        return this.delegate.getLastModifiedTime();
    }

    private final Entry delegate;
    private Set<PosixFilePermission> permissions = new HashSet<>();
}
