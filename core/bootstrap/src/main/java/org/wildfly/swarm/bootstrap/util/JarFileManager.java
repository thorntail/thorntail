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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * @author Juan Gonzalez
 */
public class JarFileManager {

    public static final JarFileManager INSTANCE = new JarFileManager();

    private JarFileManager() {
    }

    public JarFile getJarFile(File file) throws IOException {
        return TempFileManager.INSTANCE.isTempFile(file) ? addJarFile(file) : new JarFile(file);
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
        IOException ex = null;
        for (JarFile jarFile : jarFileToClose.values()) {
            try {
                jarFile.close();
            } catch (IOException e) {
                ex = e;
            }
        }

        if (ex != null) {
            throw ex;
        }
    }

    private Map<File, JarFile> jarFileToClose = new LinkedHashMap<>();
}
