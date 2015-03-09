package org.jboss.modules;

import org.jboss.modules.filter.PathFilters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class SelfContainedModuleFinder implements ModuleFinder {

    JarFile jarFile;

    public SelfContainedModuleFinder() throws IOException {
        this.jarFile = Util.rootJar();
    }

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if (identifier.getName().equals("APP")) {
            try {
                return findAppModule();
            } catch (IOException e) {
                e.printStackTrace();
                throw new ModuleLoadException(e);
            }
        }

        if (identifier.getName().equals("org.wildfly.self-contained")) {
            try {
                return findSelfContainedModule();
            } catch (IOException e) {
                e.printStackTrace();
                throw new ModuleLoadException(e);
            }
        }

        String namePath = identifier.getName().replace('.', '/');
        String basePath = "modules/system/layers/base/" + namePath + "/" + identifier.getSlot();
        JarEntry moduleXmlEntry = jarFile.getJarEntry(basePath + "/module.xml");
        if (moduleXmlEntry == null) {
            return null;
        }
        ModuleSpec moduleSpec;
        try {
            InputStream inputStream = jarFile.getInputStream(moduleXmlEntry);
            try {
                moduleSpec = ModuleXmlParser.parseModuleXml(new ModuleXmlParser.ResourceRootFactory() {
                    public ResourceLoader createResourceLoader(final String rootPath, final String loaderPath, final String loaderName) throws IOException {
                        return new JarFileResourceLoader(loaderName, jarFile, rootPath + "/" + loaderPath);
                    }
                }, basePath, inputStream, moduleXmlEntry.getName(), delegateLoader, identifier);
            } finally {
                StreamUtil.safeClose(inputStream);
            }
        } catch (IOException e) {
            throw new ModuleLoadException("Failed to read module.xml file", e);
        }
        return moduleSpec;
    }

    private ModuleSpec findSelfContainedModule() throws IOException {
        ModuleSpec.Builder builder = ModuleSpec.build(ModuleIdentifier.create("org.wildfly.self-contained"));

        ResourceLoader jar = ArtifactLoaderFactory.INSTANCE.getLoader("org.wildfly.self-contained:wildfly-self-contained:1.0.0.Beta1-SNAPSHOT");
        builder.addResourceRoot( ResourceLoaderSpec.createResourceLoaderSpec( jar ) );
        builder.addDependency(DependencySpec.createLocalDependencySpec());
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.as.self-contained"), false));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.as.server"), false));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.as.controller"), false));

        return builder.create();
    }

    private ModuleSpec findAppModule() throws IOException {
        ModuleSpec.Builder builder = ModuleSpec.build(ModuleIdentifier.create("APP"));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("app/app.jar");

        if (in == null) {
            return null;
        }


        try {
            File tmp = File.createTempFile("app", ".jar");

            FileOutputStream out = new FileOutputStream(tmp);

            try {
                byte[] buf = new byte[1024];
                int len = -1;

                while ((len = in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }

            ResourceLoader resourceLoader = new JarFileResourceLoader("app.jar", new JarFile(tmp), "WEB-INF/classes");
            ResourceLoaderSpec resourceLoaderSpec = ResourceLoaderSpec.createResourceLoaderSpec(resourceLoader);
            builder.addResourceRoot(resourceLoaderSpec);

            resourceLoader = new JarFileResourceLoader("app.jar", new JarFile(tmp));
            resourceLoaderSpec = ResourceLoaderSpec.createResourceLoaderSpec(resourceLoader);
            builder.addResourceRoot(resourceLoaderSpec);

        } finally {
            in.close();
        }


        builder.addDependency(DependencySpec.createLocalDependencySpec());
        //builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.as.self-contained"), false));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.wildfly.self-contained"), true));
        //builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.as.server"), true));

        ModuleSpec moduleSpec = builder.create();

        return moduleSpec;
    }
}
