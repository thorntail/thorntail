/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.container.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.env.FractionManifest;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.internal.ServerBootstrap;
import org.wildfly.swarm.container.runtime.cdi.ConfigViewProducingExtension;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContextImpl;
import org.wildfly.swarm.container.runtime.cdi.DeploymentScopedExtension;
import org.wildfly.swarm.container.runtime.cdi.FractionProducingExtension;
import org.wildfly.swarm.container.runtime.cdi.ImplicitArchiveExtension;
import org.wildfly.swarm.container.runtime.cdi.InterfaceExtension;
import org.wildfly.swarm.container.runtime.cdi.OutboundSocketBindingExtension;
import org.wildfly.swarm.container.runtime.cdi.SocketBindingExtension;
import org.wildfly.swarm.container.runtime.cdi.XMLConfigProducingExtension;
import org.wildfly.swarm.container.runtime.cdi.configurable.ConfigurableExtension;
import org.wildfly.swarm.container.runtime.cli.CommandLineArgsExtension;
import org.wildfly.swarm.internal.OutboundSocketBindingRequest;
import org.wildfly.swarm.internal.SocketBindingRequest;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.internal.SwarmMetricsMessages;
import org.wildfly.swarm.spi.api.ClassLoading;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.config.ConfigView;

/**
 * @author Bob McWhirter
 */
public class ServerBootstrapImpl implements ServerBootstrap {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm");

    @Override
    public ServerBootstrap withArguments(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public ServerBootstrap withXmlConfig(Optional<URL> url) {
        this.xmlConfigURL = url;
        return this;
    }

    @Override
    public ServerBootstrap withConfigView(ConfigView configView) {
        this.configView = configView;
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
    public ServerBootstrap withSocketBindings(List<SocketBindingRequest> bindings) {
        this.socketBindings = bindings;
        return this;
    }

    @Override
    public ServerBootstrap withOutboundSocketBindings(List<OutboundSocketBindingRequest> bindings) {
        this.outboundSocketBindings = bindings;
        return this;
    }

    @Override
    public Server bootstrap() throws Exception {
        try (AutoCloseable bootstrap = Performance.time("Bootstrap")) {
            Module module = Module.getBootModuleLoader().loadModule("swarm.container");
            return ClassLoading.withTCCL(new ExtensionPreventionClassLoaderWrapper(module.getClassLoader()), () -> {
                //Thread.currentThread().setContextClassLoader(new ExtensionPreventionClassLoaderWrapper(module.getClassLoader()));

                try (AutoCloseable logFractionHandle = Performance.time("Log fractions")) {
                    logFractions();
                }

                RuntimeServer outerServer = LogSilencer.silently("org.jboss.weld").execute(() -> {
                    Weld weld = new Weld(WELD_INSTANCE_ID);
                    weld.setClassLoader(module.getClassLoader());

                    ConfigViewProducingExtension configViewProducingExtension = new ConfigViewProducingExtension(this.configView);
                    DeploymentContext deploymentContext = new DeploymentContextImpl();
                    ConfigurableManager configurableManager = new ConfigurableManager(this.configView, deploymentContext);

                    // Add Extension that adds User custom bits into configurator
                    weld.addExtension(new FractionProducingExtension(explicitlyInstalledFractions, configurableManager));
                    weld.addExtension(new ConfigurableExtension(configurableManager));
                    weld.addExtension(new CommandLineArgsExtension(args));
                    weld.addExtension(configViewProducingExtension);
                    weld.addExtension(new XMLConfigProducingExtension(this.xmlConfigURL));
                    weld.addExtension(new InterfaceExtension(this.configView));
                    weld.addExtension(new OutboundSocketBindingExtension(this.outboundSocketBindings));
                    weld.addExtension(new SocketBindingExtension(this.socketBindings));
                    weld.addExtension(new DeploymentScopedExtension(deploymentContext));
                    weld.addExtension(new ImplicitArchiveExtension());


                    for (Class<?> each : this.userComponents) {
                        weld.addBeanClass(each);
                    }

                    weld.property("org.jboss.weld.se.shutdownHook", false);
                    WeldContainer weldContainer = null;
                    RuntimeServer server = null;
                    try (AutoCloseable weldRelated = Performance.time("Weld-related")) {
                        try (AutoCloseable weldInitHandle = Performance.time("Weld initialize")) {
                            weldContainer = weld.initialize();
                        }
                        try (AutoCloseable serverSelectHandle = Performance.time("Server construction")) {
                            server = weldContainer.select(RuntimeServer.class).get();
                        }
                    }
                    return server;
                });

                try (AutoCloseable weldInitHandle = Performance.time("Server start")) {
                    outerServer.start(true);
                }
                return outerServer;
            });
        } finally {
            SwarmMetricsMessages.MESSAGES.bootPerformance(Performance.dump());
        }
    }

    protected void logFractions() throws IOException {
        ApplicationEnvironment.get().fractionManifests()
                .forEach(this::logFraction);
    }

    protected void logFraction(FractionManifest manifest) {
        if (manifest.isInternal()) {
            LOG.debug(SwarmMessages.MESSAGES.availableFraction(manifest.getName(), manifest.getStabilityLevel(), manifest.getGroupId(), manifest.getArtifactId(), manifest.getVersion()));
        } else {
            int stabilityIndex = manifest.getStabilityIndex();
            if (stabilityIndex < 3) {
                LOG.warn(SwarmMessages.MESSAGES.availableFraction(manifest.getName(), manifest.getStabilityLevel(), manifest.getGroupId(), manifest.getArtifactId(), manifest.getVersion()));
            } else {
                LOG.info(SwarmMessages.MESSAGES.availableFraction(manifest.getName(), manifest.getStabilityLevel(), manifest.getGroupId(), manifest.getArtifactId(), manifest.getVersion()));
            }
        }
    }

    private String[] args;

    private Collection<Fraction> explicitlyInstalledFractions;

    private Set<Class<?>> userComponents;

    private Optional<URL> xmlConfigURL = Optional.empty();

    private boolean bootstrapDebug;

    private List<SocketBindingRequest> socketBindings;

    private List<OutboundSocketBindingRequest> outboundSocketBindings;

    private ConfigView configView;
}
