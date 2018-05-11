package io.thorntail.plugins.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import bin.Run;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

/**
 * Created by bob on 2/13/18.
 */
public class PlanExporter {

    public static void export(Plan plan, Path destination) throws Exception {
        if (destination.getFileName().toString().endsWith(".zip") || destination.getFileName().toString().endsWith(".jar")) {
            exportFile(plan, destination);
        } else {
            exportDirectory(plan, destination);
        }
    }

    static void exportFile(Plan plan, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());

        Set<Path> seen = new HashSet<>();

        try (JarArchiveOutputStream jar = new JarArchiveOutputStream(new FileOutputStream(destination.toFile()))) {
            for (Entry entry : plan) {
                Path path = entry.getPath();
                if (!seen.contains(path.getParent())) {
                    JarArchiveEntry jarDirEntry = new JarArchiveEntry(jarPath(path.getParent()) + "/");
                    jar.putArchiveEntry(jarDirEntry);
                    jar.closeArchiveEntry();
                    seen.add(path.getParent());
                }

                JarArchiveEntry jarFileEntry = new JarArchiveEntry(jarPath(path));
                jarFileEntry.setUnixMode(getMode(entry.getPermissions()));
                jar.putArchiveEntry(jarFileEntry);
                copy(entry.openStream(), jar);
                jar.closeArchiveEntry();
            }
        }
    }

    static int getMode(Set<PosixFilePermission> permissions) {
        int mode = 0;

        for (PosixFilePermission permission : permissions) {
            switch (permission) {
                case OWNER_READ:
                    mode = mode | 0400;
                    break;
                case OWNER_WRITE:
                    mode = mode | 0200;
                    break;
                case OWNER_EXECUTE:
                    mode = mode | 0100;
                    break;
                case GROUP_READ:
                    mode = mode | 0040;
                    break;
                case GROUP_WRITE:
                    mode = mode | 0020;
                    break;
                case GROUP_EXECUTE:
                    mode = mode | 0010;
                    break;
                case OTHERS_READ:
                    mode = mode | 0004;
                    break;
                case OTHERS_WRITE:
                    mode = mode | 0002;
                    break;
                case OTHERS_EXECUTE:
                    mode = mode | 0001;
                    break;
            }
        }

        if (mode == 0) {
            mode = 0444;
        }

        return mode;
    }

    static private void copy(InputStream in, JarArchiveOutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len = 0;

        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        out.flush();
    }

    static String jarPath(Path path) {
        return path.toString().replace(File.separatorChar, '/');
    }


    static void exportDirectory(Plan plan, Path destination) throws IOException, NoSuchAlgorithmException {

        Files.createDirectories(destination);

        for (Entry entry : plan) {
            Path dest = destination.resolve(entry.getPath());
            if (Files.exists(dest)) {
                // Same filename
                byte[] destHash = Run.hashOf(dest);
                byte[] srcHash = Run.hashOf(entry.openStream());
                if (Arrays.equals(destHash, srcHash)) {
                    // Same hash
                    continue;
                }
            }
            Files.createDirectories(dest.getParent());
            Files.copy(entry.openStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            if (!entry.getPermissions().isEmpty()) {
                if (!isWindows()) {
                    Files.setPosixFilePermissions(dest, entry.getPermissions());
                }
            }

            Files.setLastModifiedTime(dest, entry.getLastModifiedTime());
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

}
