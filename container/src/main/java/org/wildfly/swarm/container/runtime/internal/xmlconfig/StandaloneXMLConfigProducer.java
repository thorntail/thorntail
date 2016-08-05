package org.wildfly.swarm.container.runtime.internal.xmlconfig;

import java.net.URL;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * @author Bob McWhirter
 */

@Singleton
public class StandaloneXMLConfigProducer {

    @Produces
    @XMLConfig
    public URL fromClassLoader() {
        try {
            Module app = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create( "swarm.application" ) );
            ClassLoader cl = app.getClassLoader();
            return cl.getResource( "standalone.xml" );
        } catch (ModuleLoadException e) {
            e.printStackTrace();
        }
        return null;
    }
}
