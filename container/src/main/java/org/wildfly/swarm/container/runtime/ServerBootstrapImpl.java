package org.wildfly.swarm.container.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.internal.ServerBootstrap;
import org.wildfly.swarm.container.runtime.cdi.FractionProducingExtension;
import org.wildfly.swarm.container.runtime.cdi.ProjectStageProducingExtension;
import org.wildfly.swarm.container.runtime.cdi.XMLConfigProducingExtension;
import org.wildfly.swarm.container.runtime.cli.CommandLineArgsExtension;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.ProjectStage;

/**
 * @author Bob McWhirter
 */
public class ServerBootstrapImpl implements ServerBootstrap {

    private static Logger LOG = Logger.getLogger( "org.wildfly.swarm" );

    @Override
    public ServerBootstrap withArguments(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public ServerBootstrap withStageConfig(Optional<ProjectStage> stageConfig) {
        this.stageConfig = stageConfig;
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

        logFractions( module.getClassLoader() );

        Weld weld = new Weld();
        weld.setClassLoader(module.getClassLoader());

        // Add Extension that adds User custom bits into configurator
        weld.addExtension(new FractionProducingExtension(explicitlyInstalledFractions));
        weld.addExtension(new CommandLineArgsExtension(args));
        weld.addExtension(new ProjectStageProducingExtension(this.stageConfig));
        weld.addExtension(new XMLConfigProducingExtension(this.xmlConfigURL));

        for (Class<?> each : this.userComponents) {
            weld.addBeanClass(each);
        }

        WeldContainer weldContainer = weld.initialize();

        RuntimeServer server = weldContainer.select(RuntimeServer.class).get();
        server.start(true);
        return server;
    }

    protected void logFractions(ClassLoader cl) throws IOException {
        Enumeration<URL> fractionProps = cl.getResources("META-INF/fraction.properties");
        while ( fractionProps.hasMoreElements() ) {
            logFraction( fractionProps.nextElement() );
        }
    }

    protected void logFraction(URL url) throws IOException {
        Properties props = new Properties();
        props.load( url.openStream() );

        int stabilityIndex = Integer.parseInt(props.getProperty("stabilityIndex"));

        String name = props.getProperty( "name" );
        String groupId = props.getProperty( "groupId" );
        String artifactId = props.getProperty( "artifactId" );
        String version = props.getProperty( "version" );
        String stabilityLevel = props.getProperty( "stabilityLevel" );

        if ( stabilityIndex < 3 ) {
            LOG.warn(SwarmMessages.MESSAGES.availableFraction(name, stabilityLevel, groupId, artifactId, version));
        } else {
            LOG.info(SwarmMessages.MESSAGES.availableFraction(name, stabilityLevel, groupId, artifactId, version));
        }
    }

    private String[] args;

    private Collection<Fraction> explicitlyInstalledFractions;

    private Set<Class<?>> userComponents;

    private Optional<ProjectStage> stageConfig = Optional.empty();

    private Optional<URL> xmlConfigURL = Optional.empty();

    private boolean bootstrapDebug;

    private String stageConfigUrl;
}
