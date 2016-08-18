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
