package org.wildfly.swarm.bootstrap.modules;

import java.io.IOException;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoader;

/**
 * @author Bob McWhirter
 */
public class BootModuleLoader extends ModuleLoader {

    public BootModuleLoader() throws IOException {
        super(new ModuleFinder[]{
                new BootstrapClasspathModuleFinder(),
                //new JBossMSCBootstrapModuleFinder(),
                new BootstrapModuleFinder(),
                new ClasspathModuleFinder(),
                new AppDependenciesModuleFinder(),
        });
    }
}
