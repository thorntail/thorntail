package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.PathFilters;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class DirLayoutAppModuleFinder implements ModuleFinder {

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if (!identifier.getName().equals("APP") || !identifier.getSlot().equals("main")) {
            return null;
        }

        String path = System.getProperty("wildfly.swarm.layout");
        if (!path.startsWith("dir:")) {
            return null;
        }

        path = path.substring(4);

        ModuleSpec.Builder builder = ModuleSpec.build(identifier);

        builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(ResourceLoaders.createFileResourceLoader("app", new File(path))));
        builder.addDependency(DependencySpec.createLocalDependencySpec());


        return builder.create();
    }
}
