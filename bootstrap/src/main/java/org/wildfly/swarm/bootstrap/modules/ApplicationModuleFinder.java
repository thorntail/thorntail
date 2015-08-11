package org.wildfly.swarm.bootstrap.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarFile;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.MavenArtifactUtil;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.ClassFilter;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilter;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.util.Layout;

/**
 * Module-finder used only for loading the first set of jars when run in an fat-jar scenario.
 *
 * @author Bob McWhirter
 */
public class ApplicationModuleFinder implements ModuleFinder {
    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {

        if (!identifier.getName().equals("swarm.application")) {
            return null;
        }

        ModuleSpec.Builder builder = ModuleSpec.build(identifier);

        try {
            if (Layout.isFatJar()) {
                gatherJarsFromJar(builder);
            } else {
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                Enumeration<URL> results = cl.getResources("wildfly-swarm-bootstrap.conf");

                while (results.hasMoreElements()) {
                    URL each = results.nextElement();
                    System.err.println("----> " + each);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(each.openStream()))) {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            System.err.println( ":: " + line );
                            if ( ! line.isEmpty() ) {
                                builder.addDependency(
                                        DependencySpec.createModuleDependencySpec(
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                ClassFilters.acceptAll(),
                                                ClassFilters.acceptAll(),
                                                null,
                                                ModuleIdentifier.create(line), false));
                                //ModuleIdentifier.create(line, "api"), false));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ModuleLoadException(e);
        }

        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.modules")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.msc")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.shrinkwrap")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("javax.api")));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        ModuleIdentifier.create("org.wildfly.swarm.container", "api"), false));

        builder.addDependency(DependencySpec.createLocalDependencySpec());

        return builder.create();
    }

    protected void gatherJarsFromJar(ModuleSpec.Builder builder) throws IOException {
        InputStream bootstrapTxt = getClass().getClassLoader().getResourceAsStream("META-INF/wildfly-swarm-application.conf");

        if (bootstrapTxt != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(bootstrapTxt))) {
                String line = null;

                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        if (line.startsWith("module:")) {
                            line = line.substring(7);
                            //public static DependencySpec createModuleDependencySpec(final PathFilter importFilter, final PathFilter exportFilter, final PathFilter resourceImportFilter, final PathFilter resourceExportFilter, final ClassFilter classImportFilter, final ClassFilter classExportFilter, final ModuleLoader moduleLoader, final ModuleIdentifier identifier, final boolean optional) {
                            builder.addDependency(
                                    DependencySpec.createModuleDependencySpec(PathFilters.acceptAll(), PathFilters.acceptAll(), PathFilters.acceptAll(), PathFilters.acceptAll(), ClassFilters.acceptAll(), ClassFilters.acceptAll(), null, ModuleIdentifier.create(line), false));
                        } else if (line.startsWith("gav:")) {
                            line = line.substring(4).trim();
                            File artifact = MavenArtifactUtil.resolveJarArtifact(line);
                            if (artifact == null) {
                                throw new IOException("Unable to locate artifact: " + line);
                            }
                            builder.addResourceRoot(
                                    ResourceLoaderSpec.createResourceLoaderSpec(
                                            ResourceLoaders.createJarResourceLoader(artifact.getName(), new JarFile(artifact))
                                    )
                            );
                        } else if (line.startsWith("path:")) {
                            line = line.substring(5).trim();

                            int slashLoc = line.lastIndexOf('/');
                            String name = line;

                            if (slashLoc > 0) {
                                name = line.substring(slashLoc + 1);
                            }

                            String ext = ".jar";
                            int dotLoc = name.lastIndexOf('.');
                            if (dotLoc > 0) {
                                ext = name.substring(dotLoc);
                                name = name.substring(0, dotLoc);
                            }

                            Path tmp = Files.createTempFile(name, ext);

                            try (InputStream artifactIn = getClass().getClassLoader().getResourceAsStream(line)) {
                                Files.copy(artifactIn, tmp, StandardCopyOption.REPLACE_EXISTING);
                            }
                            builder.addResourceRoot(
                                    ResourceLoaderSpec.createResourceLoaderSpec(
                                            ResourceLoaders.createJarResourceLoader(tmp.getFileName().toString(), new JarFile(tmp.toFile()))
                                    )
                            );
                        }
                    }
                }
            }
        }
    }
}
