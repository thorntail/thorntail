package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;

/**
 * @author Bob McWhirter
 */
public class AppDependenciesModuleFinder implements ModuleFinder {
    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if (!(identifier.getName().equals("APP") && identifier.getSlot().equals("dependencies"))) {
            return null;
        }

        ModuleSpec.Builder builder = ModuleSpec.build( identifier );
        return builder.create();
    }
}
