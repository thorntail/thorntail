/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.bootstrap.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Juan Gonzalez
 */

public class BootstrapUtil {

    private BootstrapUtil() {
    }

    /**
     * Extracts a jar file into a target destination directory
     *
     * @param jarFile
     * @param destDir
     * @throws IOException
     */
    public static void explodeJar(JarFile jarFile, String destDir) throws IOException {
        Enumeration<java.util.jar.JarEntry> enu = jarFile.entries();
        while (enu.hasMoreElements()) {
            JarEntry je = enu.nextElement();

            File fl = new File(destDir, je.getName());
            if (!fl.exists()) {
                fl.getParentFile().mkdirs();
                fl = new File(destDir, je.getName());
            }
            if (je.isDirectory()) {
                continue;
            }
            InputStream is = null;
            try {
                is = jarFile.getInputStream(je);
                Files.copy(is, fl.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    public static void convertSwarmSystemPropertiesToThorntail() {
        for (String systemProperty : System.getProperties().stringPropertyNames()) {
            if (systemProperty.startsWith("swarm.")) {
                String corresponding = systemProperty.replaceFirst("^swarm\\.", "thorntail.");
                if (System.getProperty(corresponding) == null) {
                    System.setProperty(corresponding, System.getProperty(systemProperty));
                }
            }
        }
    }
}
