package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSingleModuleFinder implements ModuleFinder {

    private final String moduleName;

    private final String moduleSlot;

    public AbstractSingleModuleFinder(String moduleName) {
        this(moduleName, "main");
    }

    public AbstractSingleModuleFinder(String moduleName, String moduleSlot) {
        this.moduleName = moduleName;
        this.moduleSlot = moduleSlot;
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + this.moduleName + ":" + this.moduleSlot + ")";
    }

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if ( ! identifier.getName().equals( this.moduleName ) || ! identifier.getSlot().equals( this.moduleSlot ) ) {
            return null;
        }

        ModuleSpec.Builder builder = ModuleSpec.build(identifier);
        buildModule( builder, delegateLoader );
        return builder.create();
    }

    public abstract void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException;
}
