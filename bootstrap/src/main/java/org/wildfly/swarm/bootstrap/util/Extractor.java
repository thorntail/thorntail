package org.wildfly.swarm.bootstrap.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author Bob McWhirter
 */
public class Extractor {

    public static Path extract(JarFile jar, String path) throws IOException {
        ZipEntry entry = jar.getEntry(path);
        if (entry == null) {
            return null;
        }

        int slashLoc = path.lastIndexOf('/');
        String name = path;

        if (slashLoc > 0) {
            name = path.substring(slashLoc + 1);
        }

        String ext = ".jar";
        int dotLoc = name.lastIndexOf('.');
        if (dotLoc > 0) {
            ext = name.substring(dotLoc);
            name = name.substring(0, dotLoc);
        }

        Path tmp = Files.createTempFile(name, ext);

        try (InputStream in = jar.getInputStream(entry)) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }

        //tmp.toFile().deleteOnExit();

        return tmp;
    }
}
