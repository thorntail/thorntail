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
package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.modules.AbstractResourceLoader;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaders;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.bootstrap.util.BootstrapUtil;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * @author Bob McWhirter
 */
public class NestedJarResourceLoader {

    private static final String JAR_SUFFIX = ".jar!";

    private NestedJarResourceLoader() {
    }

    public static synchronized boolean requiresExplosion(URL base) throws IOException {
        try (AutoCloseable locateHandle = Performance.accumulate("Is explosion needed?")) {
            String urlString = base.toExternalForm();
            if (urlString.startsWith("jar:file:")) {
                int endLoc = urlString.indexOf(JAR_SUFFIX);
                if (endLoc > 0) {
                    String jarPath = urlString.substring(9, endLoc + 4);
                    //if it has spaces or other characters that would be URL encoded we need to decode them
                    jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name());

                    File exp = exploded.get(jarPath);
                    if (exp != null) {
                        return true;
                    }
                    if (explosionNotRequired.contains(jarPath)) {
                        return false;
                    }
                    try (JarFile jarFile = new JarFile(jarPath)) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry each = entries.nextElement();
                            if (!each.isDirectory()) {
                                if (each.getName().startsWith("modules") && !each.getName().endsWith("/module.xml")) {
                                    return true;
                                }
                            }
                        }
                    }
                    explosionNotRequired.add(jarPath);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static synchronized Path explodedJar(URL base) throws IOException {
        if (!requiresExplosion(base)) {
            return null;
        }
        try (AutoCloseable locateHandle = Performance.accumulate("Exploded JAR locating")) {
            String urlString = base.toExternalForm();
            if (urlString.startsWith("jar:file:")) {
                int endLoc = urlString.indexOf(JAR_SUFFIX);
                if (endLoc > 0) {
                    String jarPath = urlString.substring(9, endLoc + 4);
                    //if it has spaces or other characters that would be URL encoded we need to decode them
                    jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name());

                    File exp = exploded.get(jarPath);
                    if (exp == null) {
                        try (AutoCloseable explodingHandle = Performance.accumulate("Exploding JAR")) {
                            exp = TempFileManager.INSTANCE.newTempDirectory("module-jar", ".jar_d");
                            try (JarFile jarFile = new JarFile(jarPath)) {
                                Enumeration<JarEntry> entries = jarFile.entries();
                                while (entries.hasMoreElements()) {
                                    JarEntry each = entries.nextElement();
                                    if (!each.isDirectory()) {
                                        File out = new File(exp, each.getName());
                                        out.getParentFile().mkdirs();
                                        InputStream in = jarFile.getInputStream(each);
                                        Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        in.close();
                                    }
                                }
                            }
                            exploded.put(jarPath, exp);
                        }
                    }

                    String remainder = urlString.substring(endLoc + JAR_SUFFIX.length());
                    if (remainder.startsWith("/") || remainder.startsWith("\\")) {
                        remainder = remainder.substring(1);
                    }

                    return exp.toPath().resolve(remainder);
                }
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResourceLoader loaderFor(URL base, String rootPath, String loaderPath, String loaderName) throws IOException {
        Path exp = explodedJar(base);

        String urlString = base.toExternalForm();

        if (exp != null) {
            int endLoc = urlString.indexOf(JAR_SUFFIX);
            if (endLoc > 0) {
                Path resourceRoot = exp.resolve(loaderPath);
                if (!Files.isDirectory(resourceRoot) && (resourceRoot.getFileName().toString().endsWith(".jar") || resourceRoot.getFileName().toString().endsWith(".war"))) {
                    final File file = resourceRoot.toFile();
                    final JarFile jarFile = new JarFile(file);

                    File tmpDir = TempFileManager.INSTANCE.newTempDirectory("nestedjarloader", null);
                    //Explode jar due to some issues in Windows on stopping (JarFiles cannot be deleted)
                    BootstrapUtil.explodeJar(jarFile, tmpDir.getAbsolutePath());

                    jarFile.close();

                    return ResourceLoaders.createFileResourceLoader(loaderName, tmpDir);
                } else {
                    return ResourceLoaders.createFileResourceLoader(loaderName, resourceRoot.toFile());
                }
            }
        } else if (urlString.startsWith("file:")) {
            if (loaderName.endsWith(".jar") || loaderName.endsWith(".war")) {
                final File file = new File(urlString.substring(5), loaderPath);
                final JarFile jarFile = new JarFile(file);

                File tmpDir = TempFileManager.INSTANCE.newTempDirectory("nestedjarloader", null);
                //Explode jar due to some issues in Windows on stopping (JarFiles cannot be deleted)
                BootstrapUtil.explodeJar(jarFile, tmpDir.getAbsolutePath());

                jarFile.close();

                return ResourceLoaders.createFileResourceLoader(loaderName, tmpDir);
            }

            return ResourceLoaders.createFileResourceLoader(
                    loaderPath,
                    new File(urlString.substring(5))
            );
        } else { // TODO remove this else branch entirely, this should never happen and throwing an exception is the right thing to do
            return new AbstractResourceLoader() {
            };
        }

        throw new IllegalArgumentException("Illegal module loader base: " + base + " // " + loaderPath + " // " + loaderName);
    }

    private static Map<String, File> exploded = new HashMap<>();

    private static Set<String> explosionNotRequired = new HashSet<>();

}
