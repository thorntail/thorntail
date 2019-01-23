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
package org.jboss.modules;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.CodeSource;
import java.util.Collections;
import java.util.jar.JarFile;

import org.wildfly.swarm.jdk.specific.JarFiles;

/**
 * Resources for the environment the application is being executed in.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class Environment {

    /**
     * Retrieves the file system to use for the environment.
     *
     * <p>
     * If running in an archive a zip file system is returned. Otherwise the {@linkplain FileSystems#getDefault()
     * default} is returned.
     * </p>
     *
     * @return the file system to use for the environment
     */
    public static FileSystem getFileSystem() {
        return Holder.FILE_SYSTEM;
    }

    /**
     * Creates a new resource loader for the environment.
     *
     * <p>
     * In an archive a {@link JarFileResourceLoader} is returned. Otherwise a file system based loader is returned.
     * </p>
     *
     * @param rootPath   the root path to the module
     * @param loaderPath the path to the module
     * @param loaderName the module name
     * @return a resource loader for the environment
     */
    public static ResourceLoader getModuleResourceLoader(final String rootPath, final String loaderPath, final String loaderName) {
        if (Holder.JAR_FILE != null) {
            return new JarFileResourceLoader(loaderName, Holder.JAR_FILE, Holder.FILE_SYSTEM.getPath(rootPath, loaderPath).toString());
        }
        return ResourceLoaders.createFileResourceLoader(loaderPath, new File(rootPath));
    }

    /**
     * Lazy holder
     */
    private static final class Holder {
        static final FileSystem FILE_SYSTEM;

        static final JarFile JAR_FILE;

        static {
            // Determine the filesystem
            final ClassLoader cl = Environment.class.getClassLoader();
            final URL pathUrl = cl.getResource(Environment.class.getName().replace('.', '/') + ".class");
            if (pathUrl == null) {
                throw new RuntimeException("Could not discover the file system needed for the environment");
            }
            final URI pathUri;
            try {
                pathUri = pathUrl.toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException("Could not create FileSystem for " + pathUrl, e);
            }
            if (pathUrl.getProtocol().startsWith("jar")) {
                try {
                    // Create the zip file system
                    FILE_SYSTEM = FileSystems.newFileSystem(pathUri, Collections.<String, String>emptyMap(), cl);
                    // Register a shutdown hook to close the file system
                    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                FILE_SYSTEM.close();
                            } catch (IOException ignore) {
                            }
                        }
                    }));
                    // Get the code source to determine the jar location
                    final CodeSource codeSource = Environment.class.getProtectionDomain().getCodeSource();
                    if (codeSource == null) {
                        throw new RuntimeException("The code source could not be determine.");
                    }
                    JAR_FILE = JarFiles.create(new File(codeSource.getLocation().toURI()));
                } catch (URISyntaxException | IOException e) {
                    throw new RuntimeException("Could not create FileSystem for " + pathUrl, e);
                }
            } else {
                JAR_FILE = null;
                FILE_SYSTEM = FileSystems.getDefault();
            }
        }
    }
}
