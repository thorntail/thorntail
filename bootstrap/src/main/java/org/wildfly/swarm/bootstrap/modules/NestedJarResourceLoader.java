/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaders;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * @author Bob McWhirter
 */
public class NestedJarResourceLoader {

    private static Map<String, File> exploded = new HashMap<>();

    public static ResourceLoader loaderFor(URL base, String rootPath, String loaderPath, String loaderName) throws IOException {

        //System.err.println( "** " + base + ", " + rootPath + ", " + loaderPath + ", " + loaderName );

        String urlString = base.toExternalForm();
        if (urlString.startsWith("jar:file:")) {
            int endLoc = urlString.indexOf(".jar!");
            if (endLoc > 0) {
                String jarPath = urlString.substring(9, endLoc + 4);

                File exp = exploded.get(jarPath);

                if (exp == null) {
                    exp = TempFileManager.INSTANCE.newTempDirectory( "module-jar", ".jar_d" );

                    JarFile jarFile = new JarFile(jarPath);

                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry each = entries.nextElement();

                        if (!each.isDirectory()) {
                            File out = new File(exp, each.getName());
                            out.getParentFile().mkdirs();
                            Files.copy(jarFile.getInputStream(each), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }

                String relativeRoot = urlString.substring(endLoc + 5);
                File resourceRoot = new File(new File(exp, relativeRoot), loaderPath);
                /*
                if ( resourceRoot.listFiles() != null ) {
                    System.err.println("@ " + resourceRoot + " --> " + Arrays.asList(resourceRoot.listFiles()));
                }
                */
                return ResourceLoaders.createFileResourceLoader(loaderName, resourceRoot);
            }
        }
        else if (urlString.startsWith("file:")) {
            return ResourceLoaders.createFileResourceLoader(
                    loaderPath,
                    new File(urlString.substring(5, urlString.length()))
            );
        }

        throw new IllegalArgumentException("Illegal module loader base: "+ base);
    }

}
