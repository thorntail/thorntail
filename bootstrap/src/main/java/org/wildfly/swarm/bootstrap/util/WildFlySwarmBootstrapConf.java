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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.PathFilter;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmBootstrapConf {

    public final static String CLASSPATH_LOCATION = "META-INF/wildfly-swarm-bootstrap.conf";

    public WildFlySwarmBootstrapConf() {

    }

    public WildFlySwarmBootstrapConf(InputStream in) throws IOException {
        read(in);
    }

    public void addEntry(MavenArtifactDescriptor entry) {
        this.entries.add(entry);
    }

    public void addEntry(String gav) throws IOException {
        String[] parts = gav.split(":");

        if (parts.length < 3 || parts.length > 4) {
            throw new IOException("Invalid GAV format: " + gav);
        }

        if (parts.length == 3) {
            addEntry(parts[0], parts[1], "jar", null, parts[2]);
        } else if (parts.length == 4) {
            addEntry(parts[0], parts[1], "jar", parts[3], parts[2]);
        }
    }

    public void addEntry(String groupId, String artifactId, String type, String classifier, String version) {
        this.entries.add(new MavenArtifactDescriptor(groupId, artifactId, type, classifier, version));
    }

    public List<? extends MavenArtifactDescriptor> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }

    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            write(out);
            out.close();
            return new String(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void write(OutputStream out) throws IOException {
        try (PrintWriter writer = new PrintWriter(out)) {
            for (MavenArtifactDescriptor entry : this.entries) {
                writer.println(entry.mscGav());
            }

            writer.flush();
        }
    }

    public void read(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    addEntry(line);
                }
            }
        }

    }

    public void apply(ModuleSpec.Builder builder) throws IOException {
        for (MavenArtifactDescriptor entry : this.entries) {
            System.err.println( "apply: " + entry );
            apply(builder, entry);
        }
    }

    void apply(ModuleSpec.Builder builder, MavenArtifactDescriptor entry) throws IOException {
        File artifact = MavenResolvers.get().resolveJarArtifact(entry.mscCoordinates());
        if (artifact == null) {
            throw new IOException("Unable to locate artifact: " + entry.mscGav());
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("adding bootstrap artifact: " + artifact.getAbsolutePath());
        }

        JarFile jar = new JarFile(artifact);
        ResourceLoader originaloader = ResourceLoaders.createJarResourceLoader(artifact.getName(), jar);

        PathFilter filter = getModuleFilter( jar );
        builder.addResourceRoot(
                ResourceLoaderSpec.createResourceLoaderSpec(
                        ResourceLoaders.createFilteredResourceLoader( filter, originaloader )
                )
        );
    }

    private PathFilter getModuleFilter(JarFile jar) {
        Set<String> paths = new HashSet<>();

        Enumeration<JarEntry> jarEntries = jar.entries();

        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if ( ! jarEntry.isDirectory() ) {
                String name = jarEntry.getName();
                if ( name.endsWith( "/module.xml" ) ) {
                    paths.add( name );
                }
            }
        }
        return PathFilters.in( paths );
    }

    private static final BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.modules.bootstrap");

    private List<MavenArtifactDescriptor> entries = new ArrayList<>();

}
