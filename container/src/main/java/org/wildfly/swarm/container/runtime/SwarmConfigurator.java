/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.log.StreamModuleLogger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.cdi.UnmanagedInstance;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.runtime.cli.CommandLineArgs;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

/**
 * @author Ken Finnigan
 */
@Singleton
public class SwarmConfigurator {

    @Inject
    @CommandLineArgs
    private String[] args;

    @Inject
    BeanManager beanManager;

    public SwarmConfigurator() {
    }

    public void init() throws Exception {
        createServer(debugBootstrap);
        determineDeploymentType();
    }

    public Server start(boolean eagerlyOpen) throws Exception {
        if (!this.running) {
            this.deployer = this.server.start(eagerlyOpen);
            this.running = true;
        }

        return this.server;
    }

    public void setStageConfig(String stageConfigUrl, Optional<ProjectStage> stageConfig) {
        if (stageConfig.isPresent()) {
            System.out.println("[INFO] Starting container with stage config source : " + stageConfigUrl);
            this.server.setStageConfig(stageConfig.get());
        }
    }

    public void setXmlConfig(Optional<URL> xmlConfig) {
        if (xmlConfig.isPresent()) {
            System.out.println("[INFO] Starting container with xml config source : " + xmlConfig.get());
            this.server.setXmlConfig(xmlConfig.get());
        }
    }

    public SwarmConfigurator iface(String name, String expression) {
        this.interfaces.add(new Interface(name, expression));
        return this;
    }

    public List<Interface> ifaces() {
        return this.interfaces;
    }

    public SwarmConfigurator socketBindingGroup(SocketBindingGroup group) {
        this.socketBindingGroups.add(group);
        return this;
    }

    public List<SocketBindingGroup> socketBindingGroups() {
        return this.socketBindingGroups;
    }

    public SocketBindingGroup getSocketBindingGroup(String name) {
        for (SocketBindingGroup each : this.socketBindingGroups) {
            if (each.name().equals(name)) {
                return each;
            }
        }

        return null;
    }

    public Map<String, List<SocketBinding>> socketBindings() {
        return this.socketBindings;
    }

    public Map<String, List<OutboundSocketBinding>> outboundSocketBindings() {
        return this.outboundSocketBindings;
    }

    public void deploy() throws DeploymentException {
        Archive<?> deployment = createDefaultDeployment();
        if (deployment == null) {
            throw new DeploymentException("Unable to create default deployment");
        } else {
            deploy(deployment);
        }
    }

    public void deploy(Archive<?> deployment) throws DeploymentException {
        if (!this.running) {
            throw new RuntimeException("The Container has not been started.");
        }

        this.deployer.deploy(deployment);
    }

    public Archive<?> createDefaultDeployment() {
        try {
            Iterator<DefaultDeploymentFactory> providerIter = Module.getBootModuleLoader()
                    .loadModule(ModuleIdentifier.create("swarm.application"))
                    .loadService(DefaultDeploymentFactory.class)
                    .iterator();

            if (!providerIter.hasNext()) {
                providerIter = ServiceLoader.load(DefaultDeploymentFactory.class, ClassLoader.getSystemClassLoader())
                        .iterator();
            }

            final Map<String, DefaultDeploymentFactory> factories = new HashMap<>();

            while (providerIter.hasNext()) {
                final DefaultDeploymentFactory factory = providerIter.next();
                final DefaultDeploymentFactory current = factories.get(factory.getType());
                if (current == null) {
                    factories.put(factory.getType(), factory);
                } else {
                    // if this one is high priority than the previously-seen factory, replace it.
                    if (factory.getPriority() > current.getPriority()) {
                        factories.put(factory.getType(), factory);
                    }
                }
            }

            DefaultDeploymentFactory factory = factories.get(determineDeploymentType());
            return factory != null ? factory.create() : ShrinkWrap.create(JARArchive.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setDebugBootstrap(boolean debugBootstrap) {
        this.debugBootstrap = debugBootstrap;
    }

    public void setWeld(WeldContainer weldContainer) {
        this.weldContainer = weldContainer;
    }

    private Archive<?> getDefaultDeployment() {
        if (defaultDeployment == null) {
            try {
                defaultDeployment = createDefaultDeployment();
            } catch (RuntimeException ex) {
                // ignore
            }
        }
        return defaultDeployment;
    }

    private ClassLoader getDefaultDeploymentClassLoader() throws Exception {
        if (defaultDeploymentClassLoader == null) {
            List<URL> urllist = new ArrayList<>();
            URL archiveURL = getDefaultDeploymentURL();
            if (archiveURL != null) {
                urllist.add(archiveURL);
                File webpath = new File(new File(archiveURL.toURI()), "WEB-INF/classes");
                if (webpath.exists()) {
                    urllist.add(webpath.toURI().toURL());
                }
            }
            URL[] urls = urllist.toArray(new URL[urllist.size()]);
            Module m1 = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
            defaultDeploymentClassLoader = new URLClassLoader(urls, m1.getClassLoader());
        }
        return defaultDeploymentClassLoader;
    }

    /**
     * @return The URL to the default deployment or null
     */
    private URL getDefaultDeploymentURL() throws IOException {
        if (defaultDeploymentURL == null) {
            Archive<?> archive = getDefaultDeployment();
            if (archive != null) {
                File tmpdir = TempFileManager.INSTANCE.newTempDirectory("deployment", ".d");

                archive.as(ExplodedExporter.class).exportExploded(tmpdir);

                defaultDeploymentURL = new File(tmpdir, archive.getName()).toURI().toURL();
            }
        }
        return defaultDeploymentURL;
    }

    public void createServer(boolean debugBootstrap) {
        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", BootModuleLoader.class.getName());
        }
        if (debugBootstrap) {
            Module.setModuleLogger(new StreamModuleLogger(System.err));
        }
        try {
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.container", "runtime"));
            Class<?> serverClass = module.getClassLoader().loadClass("org.wildfly.swarm.container.runtime.RuntimeServer");

            UnmanagedInstance serverInstance = new UnmanagedInstance(serverClass.newInstance(), beanManager);
            this.server = (Server) serverInstance.inject().postConstruct().get();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    void socketBinding(SocketBinding binding) {
        socketBinding("default-sockets", binding);
    }

    void socketBinding(String groupName, SocketBinding binding) {
        List<SocketBinding> list = this.socketBindings.get(groupName);

        if (list == null) {
            list = new ArrayList<>();
            this.socketBindings.put(groupName, list);
        }

        for (SocketBinding each : list) {
            if (each.name().equals(binding.name())) {
                throw new RuntimeException("Socket binding '" + binding.name() + "' already configured for '" + each.portExpression() + "'");
            }
        }

        list.add(binding);
    }

    void outboundSocketBinding(OutboundSocketBinding binding) {
        outboundSocketBinding("default-sockets", binding);
    }

    void outboundSocketBinding(String groupName, OutboundSocketBinding binding) {
        List<OutboundSocketBinding> list = this.outboundSocketBindings.get(groupName);

        if (list == null) {
            list = new ArrayList<>();
            this.outboundSocketBindings.put(groupName, list);
        }

        for (OutboundSocketBinding each : list) {
            if (each.name().equals(binding.name())) {
                throw new RuntimeException("Outbound socket binding '" + binding.name() + "' already configured for '" + each.remoteHostExpression() + ":" + each.remotePortExpression() + "'");
            }
        }

        list.add(binding);
    }

    private String determineDeploymentType() {
        if (this.defaultDeploymentType == null) {
            this.defaultDeploymentType = determineDeploymentTypeInternal();
            System.setProperty(BootstrapProperties.DEFAULT_DEPLOYMENT_TYPE, this.defaultDeploymentType);
        }
        return this.defaultDeploymentType;
    }

    private String determineDeploymentTypeInternal() {
        String artifact = System.getProperty(BootstrapProperties.APP_PATH);
        if (artifact != null) {
            int dotLoc = artifact.lastIndexOf('.');
            if (dotLoc >= 0) {
                return artifact.substring(dotLoc + 1);
            }
        }

        artifact = System.getProperty(BootstrapProperties.APP_ARTIFACT);
        if (artifact != null) {
            int dotLoc = artifact.lastIndexOf('.');
            if (dotLoc >= 0) {
                return artifact.substring(dotLoc + 1);
            }
        }

        // fallback to file system
        FileSystemLayout fsLayout = FileSystemLayout.create();

        return fsLayout.determinePackagingType();
    }

    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();

    private Map<String, List<SocketBinding>> socketBindings = new HashMap<>();

    private Map<String, List<OutboundSocketBinding>> outboundSocketBindings = new HashMap<>();

    private List<Interface> interfaces = new ArrayList<>();

    private Server server;

    private Deployer deployer;

    private boolean running = false;

    private Boolean debugBootstrap;

    private String defaultDeploymentType;

    private Archive<?> defaultDeployment;

    private URL defaultDeploymentURL;

    private ClassLoader defaultDeploymentClassLoader;

    private WeldContainer weldContainer;
}
