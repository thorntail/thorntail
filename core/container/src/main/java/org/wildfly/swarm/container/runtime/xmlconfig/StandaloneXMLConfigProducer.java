/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.container.runtime.xmlconfig;

import java.net.URL;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.internal.SwarmMessages;

/**
 * Produces auto-discovered XML configuration (standalone.xml) URLs.
 *
 * @author Bob McWhirter
 */
@Singleton
public class StandaloneXMLConfigProducer {

    @Produces
    @XMLConfig
    public URL fromSwarmApplicationModule() {
        try {
            Module app = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
            ClassLoader cl = app.getClassLoader();
            URL result = cl.getResource("standalone.xml");
            if (result != null) {
                SwarmMessages.MESSAGES.loadingStandaloneXml("'swarm.application' module", result.toExternalForm());
            }
            return result;
        } catch (ModuleLoadException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Produces
    @XMLConfig
    public URL fromClassLoader() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL result = cl.getResource("standalone.xml");
        if (result != null) {
            SwarmMessages.MESSAGES.loadingStandaloneXml("system classloader", result.toExternalForm());
        }
        return result;
    }
}
