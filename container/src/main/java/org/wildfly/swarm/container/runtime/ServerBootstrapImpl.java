package org.wildfly.swarm.container.runtime;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.internal.ServerBootstrap;
import org.wildfly.swarm.container.runtime.cli.CommandLineArgsExtension;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.ProjectStage;

/**
 * @author Bob McWhirter
 */
public class ServerBootstrapImpl implements ServerBootstrap {

    @Override
    public ServerBootstrap withArguments(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public ServerBootstrap withStageConfig(Optional<ProjectStage> stageConfig) {
        this.stageConfig = stageConfig;
        this.stageConfigSet = true;
        return this;
    }

    @Override
    public ServerBootstrap withStageConfigUrl(String stageConfigUrl) {
        this.stageConfigUrl = stageConfigUrl;
        return this;
    }

    @Override
    public ServerBootstrap withXmlConfig(Optional<URL> url) {
        this.xmlConfigURL = url;
        return this;
    }

    @Override
    public ServerBootstrap withBootstrapDebug(boolean debugBootstrap) {
        this.bootstrapDebug = debugBootstrap;
        return this;
    }

    @Override
    public ServerBootstrap withExplicitlyInstalledFractions(Collection<Fraction> explicitlyInstalledFractions) {
        this.explicitlyInstalledFractions = explicitlyInstalledFractions;
        return this;
    }

    @Override
    public ServerBootstrap withUserComponents(Set<Class<?>> userComponentClasses) {
        this.userComponents = userComponentClasses;
        return this;
    }

    @Override
    public Server bootstrap() throws Exception {
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.container"));
        Thread.currentThread().setContextClassLoader(module.getClassLoader());

        Weld weld = new Weld();
        weld.setClassLoader(module.getClassLoader());

        // Add Extension that adds User custom bits into configurator
        weld.addExtension(new FractionProducingExtension(explicitlyInstalledFractions));
        weld.addExtension(new CommandLineArgsExtension(args));

        for (Class<?> each : this.userComponents) {
            System.err.println("adding component class: " + each);
            weld.addBeanClass(each);
        }

        WeldContainer weldContainer = weld.initialize();
        SwarmConfigurator swarmConfigurator = weldContainer.select(SwarmConfigurator.class).get();
        swarmConfigurator.setWeld(weldContainer);
        swarmConfigurator.setDebugBootstrap(this.bootstrapDebug);
        swarmConfigurator.init();
        swarmConfigurator.setXmlConfig(this.xmlConfigURL);

        if (this.stageConfigSet) {
            swarmConfigurator.setStageConfig(this.stageConfigUrl, this.stageConfig);
        }

        return swarmConfigurator.start(true);
    }

    private String[] args;

    private Collection<Fraction> explicitlyInstalledFractions;

    private Set<Class<?>> userComponents;

    private Optional<ProjectStage> stageConfig;

    private Optional<URL> xmlConfigURL;

    private boolean bootstrapDebug;

    private String stageConfigUrl;

    private boolean stageConfigSet = false;
}
