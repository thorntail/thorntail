package io.thorntail.plugins.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Created by bob on 2/13/18.
 */
public interface Entry {
    Path getPath();
    InputStream openStream() throws IOException;

    Entry filter(String key, String value);
    Entry withPermissions(PosixFilePermission...permissions);
    Set<PosixFilePermission> getPermissions();
    FileTime getLastModifiedTime();
}
