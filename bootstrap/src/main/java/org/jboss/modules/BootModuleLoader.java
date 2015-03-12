package org.jboss.modules;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class BootModuleLoader extends ModuleLoader {

    public BootModuleLoader() throws IOException {
        super( new ModuleFinder[] { new BootModuleFinder() } );
    }
}
