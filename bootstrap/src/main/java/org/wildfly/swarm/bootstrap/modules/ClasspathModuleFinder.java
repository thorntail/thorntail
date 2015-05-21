package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.Environment;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ModuleXmlParserBridge;
import org.jboss.modules.ResourceLoader;
import org.wildfly.swarm.bootstrap.util.Layout;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Bob McWhirter
 */
public class ClasspathModuleFinder implements ModuleFinder {

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        final String path = "modules/" + identifier.getName().replace('.', '/') + "/" + identifier.getSlot() + "/module.xml";

        ClassLoader cl = Layout.getBootstrapClassLoader();
        InputStream in = cl.getResourceAsStream(path);

        if (in == null && cl != ClasspathModuleFinder.class.getClassLoader()) {
            in = ClasspathModuleFinder.class.getClassLoader().getResourceAsStream(path);
        }

        if (in == null) {
            return null;
        }

        ModuleSpec moduleSpec = null;
        try {

            moduleSpec = ModuleXmlParserBridge.parseModuleXml(new ModuleXmlParserBridge.ResourceRootFactoryBridge() {
                @Override
                public ResourceLoader createResourceLoader(final String rootPath, final String loaderPath, final String loaderName) throws IOException {
                    return Environment.getModuleResourceLoader(rootPath, loaderPath, loaderName);
                }
            }, "/", in, path.toString(), delegateLoader, identifier);

        } catch (IOException e) {
            throw new ModuleLoadException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new ModuleLoadException(e);
            }
        }
        return moduleSpec;

    }
}
