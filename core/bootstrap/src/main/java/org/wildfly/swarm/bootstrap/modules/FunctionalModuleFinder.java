package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;

/**
 * Created by bob on 4/27/17.
 */
@FunctionalInterface
public interface FunctionalModuleFinder {

    ModuleSpec findModule(String name, ModuleLoader delegateLoader) throws ModuleLoadException;
}
