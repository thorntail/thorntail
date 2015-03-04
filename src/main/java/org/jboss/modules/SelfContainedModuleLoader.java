package org.jboss.modules;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class SelfContainedModuleLoader extends ModuleLoader {

    public SelfContainedModuleLoader() throws IOException {
        super( new ModuleFinder[] { new SelfContainedModuleFinder() } );
    }
}
