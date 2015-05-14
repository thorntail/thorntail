package org.jboss.modules;

import org.wildfly.swarm.bootstrap.modules.DirLayoutAppModuleFinder;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class BootModuleLoader extends ModuleLoader {

    public BootModuleLoader() throws IOException {
        super( new ModuleFinder[] {
                new DirLayoutAppModuleFinder(),
                new ClasspathModuleFinder(),
                new BootModuleFinder(),
        } );
    }
}
