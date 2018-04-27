package io.thorntail.plugins.common;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

/**
 * Created by bob on 2/13/18.
 */
abstract class AbstractEntry implements Entry {

    AbstractEntry(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }

    public Entry filter(String key, String value) {
        return new FilteredEntry(this, key, value);
    }

    @Override
    public Entry withPermissions(PosixFilePermission... permissions) {
        return new EntryWithPermissions( this, permissions );
    }

    @Override
    public Set<PosixFilePermission> getPermissions() {
        return Collections.emptySet();
    }

    @Override
    public FileTime getLastModifiedTime() {
        return FileTime.from(Instant.now());
    }

    private final Path path;
}
