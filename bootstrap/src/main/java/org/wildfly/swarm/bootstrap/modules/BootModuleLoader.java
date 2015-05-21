package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.BootModuleFinder;
import org.jboss.modules.ClasspathModuleFinder;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoader;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class BootModuleLoader extends ModuleLoader {

    public BootModuleLoader() throws IOException {
        super( new ModuleFinder[] {
                new JBossModulesBootstrapModuleFinder(),
                new BootstrapModuleFinder(),
                //new DirLayoutAppModuleFinder(),
                new ClasspathModuleFinder(),
                new AppDependenciesModuleFinder(),
                //new BootModuleFinder(),
        } );
    }
}
