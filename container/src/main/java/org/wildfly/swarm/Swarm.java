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
package org.wildfly.swarm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;

import javax.enterprise.inject.Vetoed;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.container.internal.SwarmConfigurator;
import org.wildfly.swarm.container.runtime.JBossLoggingManager;
import org.wildfly.swarm.internal.ArtifactManager;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.internal.SwarmInternalProperties;

/**
 * Default {@code main(...)} if an application does not provide one.
 *
 * <p>This simply constructs a default container, starts it and performs
 * a default deployment.  Typically only useful for barren WAR applications.</p>
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Vetoed
public class Swarm {

    public static final String VERSION;

    public static ArtifactManager ARTIFACT_MANAGER;

    public static String[] COMMAND_LINE_ARGS;

    /**
     * Construct a new, un-started container.
     *
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Swarm() throws Exception {
        this(Boolean.getBoolean(SwarmProperties.DEBUG_BOOTSTRAP));
    }

    /**
     * Construct a new, un-started container.
     *
     * @param debugBootstrap - flag to indicate if the module layer should be put into bootstrap debug mode. Same as
     *                       the jboss-module -debuglog mode which enables trace logging to System.out during the
     *                       initial bootstrap of the module layer.
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Swarm(boolean debugBootstrap) throws Exception {
        this(debugBootstrap, new String[]{});
    }

    /**
     * Construct a new, un-started container, configured using command-line arguments.
     *
     * @param args The command-line arguments arguments
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Swarm(String... args) throws Exception {
        this(false, args);
    }

    /**
     * Construct a new, un-started container, configured using command-line arguments.
     *
     * @param debugBootstrap - flag to indicate if the module layer should be put into bootstrap debug mode. Same as
     *                       the jboss-module -debuglog mode which enables trace logging to System.out during the
     *                       initial bootstrap of the module layer.
     * @param args           The command-line arguments arguments
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Swarm(boolean debugBootstrap, String... args) throws Exception {
        System.setProperty(SwarmInternalProperties.VERSION, VERSION);
        COMMAND_LINE_ARGS = args;

        // Need to setup Logging here so that Weld doesn't default to JUL.
        try {
            Module loggingModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.logging", "runtime"));

            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(loggingModule.getClassLoader());
                System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
                System.setProperty("org.jboss.logmanager.configurator", "org.wildfly.swarm.container.runtime.LoggingConfigurator");
                //force logging init
                LogManager.getLogManager();
                BootstrapLogger.setBackingLoggerManager(new JBossLoggingManager());
            } finally {
                Thread.currentThread().setContextClassLoader(originalCl);
            }
        } catch (ModuleLoadException e) {
            System.err.println("[WARN] logging not available, logging will not be configured");
        }


        Weld weld = new Weld();
        if (Boolean.getBoolean("swarm.isuberjar")) {
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
            weld.setClassLoader(module.getClassLoader());
        }

        WeldContainer weldContainer = weld.initialize();
        swarmConfigurator = weldContainer.select(SwarmConfigurator.class).get();
        swarmConfigurator.setWeld(weldContainer);
        swarmConfigurator.setDebugBootstrap(debugBootstrap);
        swarmConfigurator.init();
    }

    public Swarm withXmlConfig(URL url) {
        swarmConfigurator.withXmlConfig(url);
        return this;
    }

    public Swarm withStageConfig(URL url) {
        swarmConfigurator.withStageConfig(url);
        return this;
    }

    public StageConfig stageConfig() {
        return swarmConfigurator.stageConfig();
    }

    public boolean hasStageConfig() {
        return swarmConfigurator.hasStageConfig();
    }

    /**
     * Add a fraction to the container.
     *
     * @param fraction The fraction to add.
     * @return The container.
     */
    public Swarm fraction(Fraction fraction) {
        //TODO Store them in a static that Weld Extension can access to make them Beans
        return this;
    }

    /**
     * Configure a network interface.
     *
     * @param name       The name of the interface.
     * @param expression The expression to define the interface.
     * @return The container.
     */
    public Swarm iface(String name, String expression) {
        swarmConfigurator.iface(name, expression);
        return this;
    }

    public List<Interface> ifaces() {
        return swarmConfigurator.ifaces();
    }

    /**
     * Configure a socket-binding-group.
     *
     * @param group The socket-binding group to add.
     * @return The container.
     */
    public Swarm socketBindingGroup(SocketBindingGroup group) {
        swarmConfigurator.socketBindingGroup(group);
        return this;
    }

    public List<SocketBindingGroup> socketBindingGroups() {
        return swarmConfigurator.socketBindingGroups();
    }

    public SocketBindingGroup getSocketBindingGroup(String name) {
        return swarmConfigurator.getSocketBindingGroup(name);
    }

    public Map<String, List<SocketBinding>> socketBindings() {
        return swarmConfigurator.socketBindings();
    }

    public Map<String, List<OutboundSocketBinding>> outboundSocketBindings() {
        return swarmConfigurator.outboundSocketBindings();
    }

    /**
     * Start the container.
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Swarm start() throws Exception {
        swarmConfigurator.start(false);
        return this;
    }

    public Swarm start(boolean eagerlyOpen) throws Exception {
        swarmConfigurator.start(eagerlyOpen);
        return this;
    }

    /**
     * Start the container with a deployment.
     * <p/>
     * <p>Effectively calls {@code start().deploy(deployment)}</p>
     *
     * @param deployment The deployment to deploy.
     * @return The container.
     * @throws Exception if an error occurs.
     * @see #start()
     * @see #deploy(Archive)
     */
    public Swarm start(Archive<?> deployment) throws Exception {
        return start().deploy(deployment);
    }

    /**
     * Stop the container, undeploying all deployments.
     *
     * @return THe container.
     * @throws Exception If an error occurs.
     */
    public Swarm stop() throws Exception {
        swarmConfigurator.stop();
        return this;
    }

    /**
     * Deploy the default WAR deployment.
     * <p/>
     * <p>For WAR-based applications, the primary WAR artifact iwll be deployed.</p>
     *
     * @return The container.
     * @throws DeploymentException if an error occurs.
     */
    public Swarm deploy() throws DeploymentException {
        swarmConfigurator.deploy();
        return this;
    }

    /**
     * Deploy an archive.
     *
     * @param deployment The ShrinkWrap archive to deploy.
     * @return The container.
     * @throws DeploymentException if an error occurs.
     */
    public Swarm deploy(Archive<?> deployment) throws DeploymentException {
        swarmConfigurator.deploy(deployment);
        return this;
    }

    /**
     * Provides access to the default ShrinkWrap deployment.
     */
    public Archive<?> createDefaultDeployment() {
        return swarmConfigurator.createDefaultDeployment();
    }

    /**
     * Main entry-point.
     *
     * @param args Ignored.
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
        COMMAND_LINE_ARGS = args;

        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", "org.wildfly.swarm.bootstrap.modules.BootModuleLoader");
        }

        //TODO Support user constructed container via annotations for testing and custom use
        Swarm swarm = new Swarm(args);
        swarm.start().deploy();
    }

    public static ArtifactManager artifactManager() throws IOException {
        if (ARTIFACT_MANAGER == null) {
            ARTIFACT_MANAGER = new ArtifactManager();
            ArtifactLookup.INSTANCE.set(ARTIFACT_MANAGER);
        }
        return ARTIFACT_MANAGER;
    }

    public static JavaArchive artifact(String gav) throws Exception {
        return artifactManager().artifact(gav);
    }

    public static JavaArchive artifact(String gav, String asName) throws Exception {
        return artifactManager().artifact(gav, asName);
    }

    public static List<JavaArchive> allArtifacts() throws Exception {
        return artifactManager().allArtifacts();
    }

    static {
        InputStream in = SwarmConfigurator.class.getClassLoader().getResourceAsStream("wildfly-swarm.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VERSION = props.getProperty("version", "unknown");
    }

    private SwarmConfigurator swarmConfigurator;
}
