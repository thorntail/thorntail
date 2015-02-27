package org.wildfly.embedded;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoader;

/**
 * @author Bob McWhirter
 */
public class SelfContainedModuleLoader extends ModuleLoader {

    public SelfContainedModuleLoader() {
        super( new ModuleFinder[] { new SelfContainedModuleFinder() } );
    }
}
