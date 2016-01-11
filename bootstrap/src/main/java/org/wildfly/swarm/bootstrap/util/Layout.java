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
package org.wildfly.swarm.bootstrap.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * @author Bob McWhirter
 */
public class Layout {

    private static Layout INSTANCE;


    private final Path root;

    private final boolean uberJar;

    private ClassLoader bootstrapClassLoader;

    public static Layout getInstance() throws IOException, URISyntaxException {
        if (INSTANCE == null) {
            INSTANCE = new Layout(Layout.class.getProtectionDomain().getCodeSource());
        }
        return INSTANCE;
    }

    Layout(CodeSource codeSource) throws IOException, URISyntaxException {
        this.root = determineRoot(codeSource);
        this.uberJar = determineIfIsUberJar();
    }

    public Path getRoot() {
        return this.root;
    }

    public boolean isUberJar() {
        return this.uberJar;
    }

    public Manifest getManifest() throws IOException {
        Path root = getRoot();
        if (isUberJar()) {
            try (JarFile jar = new JarFile(root.toFile())) {
                ZipEntry entry = jar.getEntry("META-INF/MANIFEST.MF");
                if (entry != null) {
                    InputStream in = jar.getInputStream(entry);
                    return new Manifest(in);
                }
            }
        }

        return null;
    }

    public synchronized ClassLoader getBootstrapClassLoader() throws ModuleLoadException {
        if ( this.bootstrapClassLoader == null ) {
            this.bootstrapClassLoader = determineBootstrapClassLoader();
        }
        return this.bootstrapClassLoader;
    }


    private Path determineRoot(CodeSource codeSource) throws IOException, URISyntaxException {
        URL location = codeSource.getLocation();
        if (location.getProtocol().equals("file")) {
            return Paths.get(location.toURI());
        }

        throw new IOException("Unable to determine root");
    }

    private boolean determineIfIsUberJar() throws IOException {
        Path root = getRoot();

        if (Files.isRegularFile(root)) {
            try (JarFile jar = new JarFile(root.toFile())) {
                ZipEntry propsEntry = jar.getEntry("META-INF/wildfly-swarm.properties");
                if (propsEntry != null) {
                    try (InputStream in = jar.getInputStream(propsEntry)) {
                        Properties props = new Properties();
                        props.load(in);
                        if (props.containsKey("wildfly.swarm.app.artifact")) {
                            System.setProperty("wildfly.swarm.app.artifact", props.getProperty("wildfly.swarm.app.artifact"));
                        }

                        Set<String> names = props.stringPropertyNames();
                        for ( String name: names ) {
                            String value = props.getProperty(name);
                            if (System.getProperty(name) == null) {
                                System.setProperty(name, value);
                            }
                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }


    private ClassLoader determineBootstrapClassLoader() throws ModuleLoadException {
        try {
            return Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap")).getClassLoader();
        } catch (ModuleLoadException e) {
            return Layout.class.getClassLoader();
        }
    }



}
