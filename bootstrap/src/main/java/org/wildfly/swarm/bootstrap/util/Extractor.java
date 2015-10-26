/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
