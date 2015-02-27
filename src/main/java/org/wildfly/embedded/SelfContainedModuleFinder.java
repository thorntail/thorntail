package org.wildfly.embedded;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.wildfly.embedded.modules.BaseModule;

/**
 * @author Bob McWhirter
 */
public class SelfContainedModuleFinder implements ModuleFinder {

    private final ArtifactLoaderFactory artifactLoaderFactory;

    public SelfContainedModuleFinder() {
        this.artifactLoaderFactory = new ArtifactLoaderFactory();
    }

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        System.err.println( "findModule: " + identifier );
        String className = "modules.system.layers.base." + identifier.getName() + "." + identifier.getSlot() + ".Module";
        System.err.println( "classname: " + className );

        try {
            Class<BaseModule> cls = (Class<BaseModule>) getClass().getClassLoader().loadClass(className);
            BaseModule module = cls.newInstance();
            return module.getModuleSpec( this.artifactLoaderFactory );
        } catch (ClassNotFoundException e) {
            throw new ModuleLoadException(e);
        } catch (InstantiationException e) {
            throw new ModuleLoadException(e);
        } catch (IllegalAccessException e) {
            throw new ModuleLoadException(e);
        } catch (Exception e) {
            throw new ModuleLoadException(e);
        }
    }
}
