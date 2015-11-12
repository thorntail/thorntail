package org.wildfly.swarm.bootstrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.MavenArtifactUtil;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmApplicationConf {


    public static final String CLASSPATH_LOCATION = "META-INF/wildfly-swarm-application.conf";

    public static abstract class Entry {

        abstract void apply(ModuleSpec.Builder builder) throws Exception ;

    }

    public static class ModuleEntry extends Entry {
        private final String name;

        ModuleEntry(String name) {
            this.name = name;
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
                            ModuleIdentifier.create(this.name), false));
        }
    }

    public static class GAVEntry extends Entry {

        private final String gav;

        GAVEntry(String gav) {
            this.gav = gav;
        }

        @Override
        void apply(ModuleSpec.Builder builder) throws IOException {
            File artifact = MavenArtifactUtil.resolveJarArtifact(this.gav);

            if (artifact == null) {
                throw new IOException("Unable to locate artifact: " + this.gav);
            }
            builder.addResourceRoot(
                    ResourceLoaderSpec.createResourceLoaderSpec(
                            ResourceLoaders.createJarResourceLoader(artifact.getName(), new JarFile(artifact))
                    )
            );
        }
    }

    public static class PathEntry extends Entry {

        private final String path;

        PathEntry(String path) {
            this.path = path;
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

            Path tmp = Files.createTempFile(name, ext);
            tmp.toFile().deleteOnExit();

            try (InputStream artifactIn = getClass().getClassLoader().getResourceAsStream(this.path)) {
                Files.copy(artifactIn, tmp, StandardCopyOption.REPLACE_EXISTING);
            }
            builder.addResourceRoot(
                    ResourceLoaderSpec.createResourceLoaderSpec(
                            ResourceLoaders.createJarResourceLoader(tmp.getFileName().toString(), new JarFile(tmp.toFile()))
                    )
            );
        }
    }

    private List<Entry> entries = new ArrayList<>();

    public WildFlySwarmApplicationConf(InputStream in) throws IOException {
        read(in);
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }

    public void apply(ModuleSpec.Builder builder) throws Exception {
        for (Entry entry : this.entries) {
            entry.apply( builder );
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
                    } else if (line.startsWith("mscGav:")) {
                        line = line.substring(4).trim();
                        entry = new GAVEntry(line);
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
}
