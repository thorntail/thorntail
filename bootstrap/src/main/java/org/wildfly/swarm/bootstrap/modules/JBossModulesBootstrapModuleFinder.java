package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class JBossModulesBootstrapModuleFinder implements ModuleFinder {
    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if (!(identifier.getName().equals("org.jboss.modules") && identifier.getSlot().equals("main"))) {
            return null;
        }

        ModuleSpec.Builder builder = ModuleSpec.build(identifier);

        Set<String> paths = new HashSet<>();

        paths.add("org/jboss/modules");
        paths.add("org/jboss/modules/log");
        paths.add("org/jboss/modules/filter");
        paths.add("org/jboss/modules/ref");
        paths.add("org/jboss/modules/management");
        paths.add("org/jboss/modules/security");

        builder.addDependency(DependencySpec.createSystemDependencySpec(paths, true));
        builder.addDependency(DependencySpec.createLocalDependencySpec());

        return builder.create();
    }
}
