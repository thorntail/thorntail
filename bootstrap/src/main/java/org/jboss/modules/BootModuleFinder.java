package org.jboss.modules;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.modules.filter.PathFilters;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class BootModuleFinder implements ModuleFinder {

    private final FileSystem fileSystem;

    public BootModuleFinder() throws IOException {
        fileSystem = Environment.getFileSystem();
    }

    @Override
    public ModuleSpec findModule(final ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if (identifier.getName().equals("APP")) {
            try {
                return findAppModule(identifier.getSlot());
            } catch (IOException e) {
                throw new ModuleLoadException(e);
            }
        }

        final String namePath = identifier.getName().replace(".", fileSystem.getSeparator());
        final Path basePath = fileSystem.getPath("modules", "system", "layers", "base", namePath, identifier.getSlot());
        final Path moduleXml = basePath.resolve("module.xml");

        if (Files.notExists(moduleXml)) {
            return null;
        }
        final ModuleSpec moduleSpec;
        try (InputStream inputStream = Files.newInputStream(moduleXml)) {
            moduleSpec = ModuleXmlParser.parseModuleXml(new ModuleXmlParser.ResourceRootFactory() {
                @Override
                public ResourceLoader createResourceLoader(final String rootPath, final String loaderPath, final String loaderName) throws IOException {
                    return Environment.getModuleResourceLoader(rootPath, loaderPath, loaderName);
                }
            }, basePath.toString(), inputStream, moduleXml.toString(), delegateLoader, identifier);
        } catch (IOException e) {
            throw new ModuleLoadException("Failed to read module.xml file", e);
        }
        return moduleSpec;
    }

    private ModuleSpec findAppModule(String slot) throws IOException {
        if (slot.equals("main")) {
            return findAppModule_main();
        } else if (slot.equals("dependencies")) {
            return findAppModule_dependencies();
        }
        return null;
    }

    private ModuleSpec findAppModule_main() throws IOException {
        final Path manifestPath = fileSystem.getPath("META-INF", "MANIFEST.MF");
        if (Files.notExists(manifestPath)) {
            return null;
        }

        final Manifest manifest = new Manifest();
        try (final InputStream in = Files.newInputStream(manifestPath)) {
            manifest.read(in);
        }
        final String artifactName = manifest.getMainAttributes().getValue("Application-Artifact");
        final Path appArtifact = fileSystem.getPath("app", artifactName);

        if (Files.notExists(appArtifact)) {
            return null;
        }
        ModuleSpec.Builder builder = ModuleSpec.build(ModuleIdentifier.create("APP"));

        String rootName = artifactName;
        String extension = ".jar";
        int dotLoc = artifactName.lastIndexOf( "." );
        if (dotLoc >= 0 ) {
            rootName = artifactName.substring( 0, dotLoc );
            extension = artifactName.substring(dotLoc);
        }

        final Path tmp = Files.createTempFile(rootName, extension);
        tmp.toFile().deleteOnExit();
        Files.copy(appArtifact, tmp, StandardCopyOption.REPLACE_EXISTING);

        if (artifactName.toLowerCase(Locale.ROOT).endsWith(".war")) {
            // Load the WAR's classes from the deployment archive
            ResourceLoader resourceLoader = new JarFileResourceLoader(artifactName, new JarFile(tmp.toFile()), "WEB-INF/classes");
            ResourceLoaderSpec resourceLoaderSpec = ResourceLoaderSpec.createResourceLoaderSpec(resourceLoader);
            builder.addResourceRoot(resourceLoaderSpec);
        }

        // Load the deployment archive itself
        ResourceLoader resourceLoader = new JarFileResourceLoader(artifactName, new JarFile(tmp.toFile()));
        ResourceLoaderSpec resourceLoaderSpec = ResourceLoaderSpec.createResourceLoaderSpec(resourceLoader);
        builder.addResourceRoot(resourceLoaderSpec);

        System.setProperty("wildfly.swarm.app.path", tmp.toAbsolutePath().toString());
        System.setProperty("wildfly.swarm.app.name", artifactName);


        builder.addDependency(DependencySpec.createLocalDependencySpec());
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.wildfly.swarm.container"), false));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("APP", "dependencies"), false));

        String modulesStr = manifest.getMainAttributes().getValue("Feature-Pack-Modules");
        String[] modules = modulesStr.split(",");
        for (final String module : modules) {
            String[] parts = module.trim().split(":");
            builder.addDependency(DependencySpec.createModuleDependencySpec(
                    PathFilters.acceptAll(),
                    PathFilters.getMetaInfServicesFilter(),
                    null,
                    ModuleIdentifier.create(parts[0], parts[1]),
                    false));
        }

        return builder.create();
    }

    private ModuleSpec findAppModule_dependencies() throws IOException {
        ModuleSpec.Builder builder = ModuleSpec.build(ModuleIdentifier.create("APP", "dependencies"));

        final Path depsTxt = fileSystem.getPath("dependencies.txt");

        if (Files.exists(depsTxt)) {
            ProjectDependencies deps = ProjectDependencies.initialize(Files.newInputStream(depsTxt));

            Set<String> gavs = deps.getGAVs();
            for ( String each : gavs ) {
                ResourceLoader loader = ArtifactLoaderFactory.INSTANCE.getLoader(each);
                builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(loader));
            }
        }
        builder.addDependency(DependencySpec.createLocalDependencySpec());

        return builder.create();
    }
}
