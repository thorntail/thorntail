/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
