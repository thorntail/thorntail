package org.wildfly.swarm.bootstrap.util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;

public class JarFileManager {

    public static final JarFileManager INSTANCE = new JarFileManager();

    private JarFileManager() {
    }

    public JarFile addJarFile(File file) throws IOException {

        JarFile jarFile = jarFileToClose.get(file);
        if (jarFile == null) {
             jarFile = new JarFile(file);
             jarFileToClose.put(file, jarFile);
        }

        return jarFile;
    }

    public void close() throws IOException {
        jarFileToClose.
                forEach((f, j) -> {
                        try {
                            j.close();
                        } catch (IOException e) {
                        }
                    }
                );
    }

    private Map<File, JarFile> jarFileToClose = new LinkedHashMap<>();
}
