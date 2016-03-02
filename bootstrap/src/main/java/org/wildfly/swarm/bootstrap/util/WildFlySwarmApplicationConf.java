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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmApplicationConf {


    public static final String CLASSPATH_LOCATION = "META-INF/wildfly-swarm-application.conf";

    public WildFlySwarmApplicationConf() {

    }

    public WildFlySwarmApplicationConf(InputStream in) throws IOException {
        read(in);
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }

    public void apply(ModuleSpec.Builder builder) throws Exception {
        for (Entry entry : this.entries) {
            entry.apply(builder);
        }
    }

    public void write(OutputStream out) {
        PrintWriter writer = new PrintWriter(out);

        for (Entry entry : this.entries) {
            entry.write(writer);
        }

        writer.flush();
    }

    public String toString() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(out);
            out.close();
            return new String(out.toByteArray());
        } catch (IOException e) {
            return "";
        }
    }

    protected void read(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    Entry entry = null;
                    if (line.startsWith("module:")) {
                        line = line.substring(7).trim();
                        entry = new ModuleEntry(line);
                    } else if (line.startsWith("gav:")) {
                        line = line.substring(4).trim();
                        entry = new GAVEntry(MavenArtifactDescriptor.fromMscGav(line));
                    } else if (line.startsWith("path:")) {
                        line = line.substring(5).trim();
                        entry = new PathEntry(line);
                    }

                    if (entry != null) {
                        this.entries.add(entry);
                    }
                }
            }
        }

    }

    private List<Entry> entries = new ArrayList<>();

    public static abstract class Entry {

        abstract void apply(ModuleSpec.Builder builder) throws Exception;

        abstract void write(PrintWriter writer);

    }

    public static class ModuleEntry extends Entry {
        public ModuleEntry(String name) {
            String[] parts = name.split(":");
            this.name = parts[0];
            if (parts.length == 2) {
                this.slot = parts[1];
            } else {
                this.slot = "main";
            }
        }

        public String getName() {
            return this.name;
        }

        @Override
        void apply(ModuleSpec.Builder builder) {
            builder.addDependency(
                    DependencySpec.createModuleDependencySpec(
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            null,
                            ModuleIdentifier.create(this.name, this.slot), false));
        }

        @Override
        void write(PrintWriter writer) {
            writer.println("module:" + this.name + ":" + this.slot);
        }

        private final String name;

        private final String slot;
    }

    public static class GAVEntry extends Entry {

        public GAVEntry(MavenArtifactDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public MavenArtifactDescriptor getDescriptor() {
            return this.descriptor;
        }

        @Override
        void apply(ModuleSpec.Builder builder) throws IOException {
            File artifact = MavenResolvers.get().resolveJarArtifact(this.descriptor.mscCoordinates());

            if (artifact == null) {
                throw new IOException("Unable to locate artifact: " + this.descriptor.mscGav());
            }
            builder.addResourceRoot(
                    ResourceLoaderSpec.createResourceLoaderSpec(
                            ResourceLoaders.createJarResourceLoader(artifact.getName(), new JarFile(artifact))
                    )
            );
        }

        @Override
        void write(PrintWriter writer) {
            writer.println("gav:" + this.descriptor.mscGav());
        }

        private final MavenArtifactDescriptor descriptor;
    }

    public static class PathEntry extends Entry {

        public PathEntry(String path) {
            this.path = path;
        }

        public String getPath() {
            return this.path;
        }

        @Override
        void apply(ModuleSpec.Builder builder) throws IOException {

            int slashLoc = this.path.lastIndexOf('/');
            String name = this.path;

            if (slashLoc > 0) {
                name = this.path.substring(slashLoc + 1);
            }

            String ext = ".jar";
            int dotLoc = name.lastIndexOf('.');
            if (dotLoc > 0) {
                ext = name.substring(dotLoc);
                name = name.substring(0, dotLoc);
            }

            File tmp = TempFileManager.INSTANCE.newTempFile(name, ext);

            try (InputStream artifactIn = getClass().getClassLoader().getResourceAsStream(this.path)) {
                Files.copy(artifactIn, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            final String jarName = tmp.getName().toString();
            final JarFile jarFile = new JarFile(tmp);
            final ResourceLoader jarLoader = ResourceLoaders.createJarResourceLoader(jarName,
                                                                                     jarFile);
            builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(jarLoader));

            if (".war".equals(ext)) {
                final ResourceLoader warLoader = ResourceLoaders.createJarResourceLoader(jarName,
                                                                                         jarFile,
                                                                                         "WEB-INF/classes");
                builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(warLoader));
            }
        }

        @Override
        void write(PrintWriter writer) {
            writer.println("path:" + this.path);
        }

        private final String path;
    }
}
