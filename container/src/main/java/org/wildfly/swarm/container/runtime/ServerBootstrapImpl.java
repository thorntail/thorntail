package org.wildfly.swarm.container.runtime;

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.internal.ServerBootstrap;
import org.wildfly.swarm.container.runtime.cli.CommandLineArgsExtension;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
public class ServerBootstrapImpl implements ServerBootstrap {

    @Override
    public Server bootstrap(String[] args, Collection<Fraction> explicitlyInstalledFractions) throws Exception {
        FractionProducingExtension.explicitlyInstalledFractions.addAll(explicitlyInstalledFractions);

        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.container"));
        Thread.currentThread().setContextClassLoader(module.getClassLoader());

        Weld weld = new Weld();
        weld.setClassLoader(module.getClassLoader());
        Enumeration<URL> res = module.getClassLoader().getResources("META-INF/beans.xml");

        // Add Extension that adds User custom bits into configurator
        weld.addExtension(new FractionProducingExtension());
        weld.addExtension(new CommandLineArgsExtension(args));

        WeldContainer weldContainer = weld.initialize();
        SwarmConfigurator swarmConfigurator = weldContainer.select(SwarmConfigurator.class).get();
        swarmConfigurator.setWeld(weldContainer);
        swarmConfigurator.setDebugBootstrap(false);
        swarmConfigurator.init();
        return swarmConfigurator.start(true);
    }

}
