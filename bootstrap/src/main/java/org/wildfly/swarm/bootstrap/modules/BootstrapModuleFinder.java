package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.*;
import org.wildfly.swarm.bootstrap.util.Layout;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Module-finder used only for loading the first set of jars when run in an fat-jar scenario.
 *
 * @author Bob McWhirter
 */
public class BootstrapModuleFinder implements ModuleFinder {
    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {

        if (!identifier.getName().equals("org.wildfly.swarm.bootstrap")) {
            return null;
        }

        ModuleSpec.Builder builder = ModuleSpec.build(identifier);

        try {
            if (Layout.isFatJar()) {
                gatherJarsFromJar(builder);
            }
        } catch (IOException e) {
            throw new ModuleLoadException(e);
        }

        builder.addDependency(DependencySpec.createLocalDependencySpec());
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.modules")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.msc")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.shrinkwrap")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("javax.api")));

        return builder.create();
    }

    protected void gatherJarsFromJar(ModuleSpec.Builder builder) throws IOException {
        InputStream bootstrapTxt = getClass().getClassLoader().getResourceAsStream("META-INF/wildfly-swarm-bootstrap.conf");

        if (bootstrapTxt != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(bootstrapTxt))) {
                String line = null;

                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        File artifact = MavenArtifactUtil.resolveJarArtifact(line);
                        if (artifact == null) {
                            throw new IOException("Unable to locate artifact: " + line);
                        }
                        builder.addResourceRoot(
                                ResourceLoaderSpec.createResourceLoaderSpec(
                                        ResourceLoaders.createJarResourceLoader(artifact.getName(), new JarFile(artifact))
                                )
                        );
                    }
                }
            }
        }
    }
}
