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
import java.util.List;
import java.util.jar.JarFile;

import org.jboss.modules.MavenArtifactUtil;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmBootstrapConf {

    public final static String CLASSPATH_LOCATION = "META-INF/wildfly-swarm-bootstrap.conf";


    private List<MavenArtifactDescriptor> entries = new ArrayList<>();


    public WildFlySwarmBootstrapConf() {

    }

    public void addEntry(MavenArtifactDescriptor entry) {
        this.entries.add( entry );
    }


    public void addEntry(String gav) throws IOException {
        String[] parts = gav.split(":");

        if (parts.length < 3 || parts.length > 4) {
            throw new IOException("Invalid GAV format: " + gav);
        }

        if (parts.length == 3) {
            addEntry( parts[0], parts[1], "jar", null, parts[2] );
        } else if (parts.length == 4) {
            addEntry( parts[0], parts[1], "jar", parts[3], parts[2] );
        }
    }

    public void addEntry(String groupId, String artifactId, String type, String classifier, String version) {
        this.entries.add(new MavenArtifactDescriptor(groupId, artifactId, type, classifier, version));
    }

    public List<? extends MavenArtifactDescriptor> getEntries() {
        return Collections.unmodifiableList( this.entries);
    }

    public WildFlySwarmBootstrapConf(InputStream in) throws IOException {
        read(in);
    }

    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            write( out );
            out.close();
            return new String( out.toByteArray() );
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
            apply( builder, entry );
        }
    }

    void apply(ModuleSpec.Builder builder, MavenArtifactDescriptor entry) throws IOException {
        File artifact = MavenArtifactUtil.resolveJarArtifact(entry.mscGav());
        if (artifact == null) {
            throw new IOException("Unable to locate artifact: " + entry.mscGav());
        }
        builder.addResourceRoot(
                ResourceLoaderSpec.createResourceLoaderSpec(
                        ResourceLoaders.createJarResourceLoader(artifact.getName(), new JarFile(artifact))
                )
        );
    }

}
