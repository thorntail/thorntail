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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.log.StreamModuleLogger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.cdi.UnmanagedInstance;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.runtime.cdi.ProjectStageImpl;
import org.wildfly.swarm.container.runtime.cli.CommandLineArgs;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.StageConfig;

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
            this.stageConfig = stageConfig;
            this.server.setStageConfig(stageConfig);
        }
    }

    @Produces
    @Singleton
    public ProjectStage projectStage() {
        if (this.stageConfig.isPresent()) {
            return this.stageConfig.get();
        }

        return new ProjectStageImpl("default");
    }

    @Produces
    @Dependent
    public StageConfig stageConfig() {
        System.err.println("*** producing stageConfig");
        return new StageConfig(projectStage());
    }

    public void setXmlConfig(Optional<URL> xmlConfig) {
        if (xmlConfig.isPresent()) {
            System.out.println("[INFO] Starting container with xml config source : " + xmlConfig.get());
            this.server.setXmlConfig(xmlConfig);
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
        if (!this.running) {
            throw new RuntimeException("Swarm has not been started.");
        }

        this.server.deploy();
    }

    public void deploy(Archive<?> deployment) throws DeploymentException {
        if (!this.running) {
            throw new RuntimeException("Swarm has not been started.");
        }

        this.server.deploy(deployment);
    }

    public void setDebugBootstrap(boolean debugBootstrap) {
        this.debugBootstrap = debugBootstrap;
    }

    public void setWeld(WeldContainer weldContainer) {
        this.weldContainer = weldContainer;
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

    private Optional<ProjectStage> stageConfig = Optional.empty();
}
