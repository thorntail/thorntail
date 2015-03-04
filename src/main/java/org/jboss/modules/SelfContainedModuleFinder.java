package org.jboss.modules;

import java.io.IOException;
import java.io.InputStream;
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
//        System.err.println( "findModule: " + identifier );
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
}
