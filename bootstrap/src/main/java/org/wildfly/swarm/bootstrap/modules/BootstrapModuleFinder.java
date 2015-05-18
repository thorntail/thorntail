package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.wildfly.swarm.bootstrap.util.Extractor;
import org.wildfly.swarm.bootstrap.util.Layout;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
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
                gatherJarsFromJar(builder, Layout.getRoot());
            }
        } catch (IOException e) {
            throw new ModuleLoadException(e);
        }

        builder.addDependency(DependencySpec.createLocalDependencySpec());
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.modules")));

        return builder.create();
    }

    protected void gatherJarsFromJar(ModuleSpec.Builder builder, Path path) throws IOException {
        JarFile jar = new JarFile(path.toFile());

        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry each = entries.nextElement();
            if (!each.isDirectory()) {
                if (each.getName().startsWith("_bootstrap")) {
                    Path member = Extractor.extract(jar, each.getName());
                    System.err.println("extracted: " + member);
                    builder.addResourceRoot(
                            ResourceLoaderSpec.createResourceLoaderSpec(
                                    ResourceLoaders.createJarResourceLoader(each.getName(), new JarFile(member.toFile()))
                            )
                    );
                    System.err.println("ADD: " + each.getName());
                }
            }
        }
    }
}
