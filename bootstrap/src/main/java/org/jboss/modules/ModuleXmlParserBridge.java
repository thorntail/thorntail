package org.jboss.modules;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Bob McWhirter
 */
public class ModuleXmlParserBridge {

    public static ModuleSpec parseModuleXml(final ResourceRootFactoryBridge factory,
                                            final String rootPath,
                                            InputStream source,
                                            final String moduleInfoFile,
                                            final ModuleLoader moduleLoader,
                                            final ModuleIdentifier moduleIdentifier) throws ModuleLoadException, IOException {
        return ModuleXmlParser.parseModuleXml(
                factory,
                rootPath,
                source,
                moduleInfoFile,
                moduleLoader,
                moduleIdentifier);

    }

    public static ResourceLoader createMavenArtifactLoader(final String name) throws IOException {
        return ModuleXmlParser.createMavenArtifactLoader(name);
    }

    public interface ResourceRootFactoryBridge extends ModuleXmlParser.ResourceRootFactory {

    }
}
