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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.LogManager;
import java.util.zip.ZipEntry;

import javax.enterprise.inject.Vetoed;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.log.StreamModuleLogger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.ExplodedExporterImpl;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.cli.CommandLine;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.internal.ServerBootstrap;
import org.wildfly.swarm.container.runtime.cdi.ProjectStageFactory;
import org.wildfly.swarm.container.runtime.logging.JBossLoggingManager;
import org.wildfly.swarm.internal.ArtifactManager;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.ProjectStage;
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

    private final CommandLine commandLine;

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
        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", BootModuleLoader.class.getName());
        }
        if (debugBootstrap) {
            Module.setModuleLogger(new StreamModuleLogger(System.err));
        }

        System.setProperty(SwarmInternalProperties.VERSION, VERSION);

        setArgs(args);
        this.debugBootstrap = debugBootstrap;

        // Need to setup Logging here so that Weld doesn't default to JUL.
        try {
            Module loggingModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.logging", "runtime"));

            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(loggingModule.getClassLoader());
                System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
                System.setProperty("org.jboss.logmanager.configurator", "org.wildfly.swarm.container.runtime.wildfly.LoggingConfigurator");
                //force logging init
                LogManager.getLogManager();
                BootstrapLogger.setBackingLoggerManager(new JBossLoggingManager());
            } finally {
                Thread.currentThread().setContextClassLoader(originalCl);
            }
        } catch (ModuleLoadException e) {
            System.err.println("[WARN] logging not available, logging will not be configured");
        }

        createShrinkWrapDomain();

        this.commandLine = CommandLine.parse(args);
        this.commandLine.apply(this);

        if (!this.stageConfig.isPresent()) {
            try {
                String stageFile = System.getProperty(SwarmProperties.PROJECT_STAGE_FILE);

                URL url = null;

                if (stageFile != null) {
                    url = new URL(stageFile);
                } else {
                    try {
                        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
                        url = module.getClassLoader().getResource("project-stages.yml");
                    } catch (ModuleLoadException e) {
                        e.printStackTrace();
                    }
                }

                if (url == null) {
                    url = ClassLoader.getSystemClassLoader().getResource("project-stages.yml");
                }

                if (url != null) {
                    this.stageConfigUrl = Optional.of(url);
                    loadStageConfiguration(url);
                }
            } catch (MalformedURLException e) {
                System.err.println("[WARN] Failed to parse project stage URL reference, ignoring: " + e.getMessage());
            }
        }
    }

    public CommandLine getCommandLine() {
        return this.commandLine;
    }

    public void setArgs(String... args) {
        this.args = args;
    }

    public Swarm withXmlConfig(URL url) {
        this.xmlConfig = Optional.of(url);
        return this;
    }

    public Swarm withStageConfig(URL url) {
        this.stageConfigUrl = Optional.of(url);

        if (!this.stageConfig.isPresent()) {
            loadStageConfiguration(stageConfigUrl.get());
        } else {
            System.out.println("[INFO] Project stage superseded by external configuration " + System.getProperty(SwarmProperties.PROJECT_STAGE_FILE));
        }
        return this;
    }

    public StageConfig stageConfig() {
        if (!stageConfig.isPresent())
            throw SwarmMessages.MESSAGES.missingStageConfig();
        return new StageConfig(stageConfig.get());
    }

    public boolean hasStageConfig() {
        return stageConfig.isPresent();
    }

    /**
     * Add a fraction to the container.
     *
     * @param fraction The fraction to add.
     * @return The container.
     */
    public Swarm fraction(Fraction fraction) {
        this.explicitlyInstalledFractions.add(fraction);
        return this;
    }

    public Swarm component(Class<?> cls) {
        this.userComponentClasses.add(cls);
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
        //swarmConfigurator.iface(name, expression);
        return this;
    }

    public List<Interface> ifaces() {
        //return swarmConfigurator.ifaces();
        return null;
    }

    /**
     * Configure a socket-binding-group.
     *
     * @param group The socket-binding group to add.
     * @return The container.
     */
    public Swarm socketBindingGroup(SocketBindingGroup group) {
        //swarmConfigurator.socketBindingGroup(group);
        return this;
    }

    public List<SocketBindingGroup> socketBindingGroups() {
        //return swarmConfigurator.socketBindingGroups();
        return null;
    }

    public SocketBindingGroup getSocketBindingGroup(String name) {
        //return swarmConfigurator.getSocketBindingGroup(name);
        return null;
    }

    public Map<String, List<SocketBinding>> socketBindings() {
        //return swarmConfigurator.socketBindings();
        return null;
    }

    public Map<String, List<OutboundSocketBinding>> outboundSocketBindings() {
        //return swarmConfigurator.outboundSocketBindings();
        return null;
    }

    /**
     * Start the container.
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Swarm start() throws Exception {
        this.start(false);
        return this;
    }

    public Swarm start(boolean eagerlyOpen) throws Exception {
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.container"));
        Class<?> bootstrapClass = module.getClassLoader().loadClass("org.wildfly.swarm.container.runtime.ServerBootstrapImpl");

        ServerBootstrap bootstrap = (ServerBootstrap) bootstrapClass.newInstance();
        bootstrap
                .withArguments(this.args)
                .withBootstrapDebug(this.debugBootstrap)
                .withExplicitlyInstalledFractions(this.explicitlyInstalledFractions)
                .withUserComponents(this.userComponentClasses)
                .withXmlConfig(this.xmlConfig);

        if (this.stageConfigUrl.isPresent() && this.stageConfig.isPresent()) {
            bootstrap
                    .withStageConfig(this.stageConfig)
                    .withStageConfigUrl(this.stageConfigUrl.get().toExternalForm());
        }

        this.server = bootstrap.bootstrap();

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
        if (this.server == null) {
            throw SwarmMessages.MESSAGES.containerNotStarted("stop()");
        }

        this.server.stop();
        this.server = null;
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
    public Swarm deploy() throws Exception {
        if (this.server == null) {
            throw SwarmMessages.MESSAGES.containerNotStarted("deploy()");
        }

        this.server.deployer().deploy();
        return this;
    }

    /**
     * Deploy an archive.
     *
     * @param deployment The ShrinkWrap archive to deploy.
     * @return The container.
     * @throws DeploymentException if an error occurs.
     */
    public Swarm deploy(Archive<?> deployment) throws Exception {
        if (this.server == null) {
            throw SwarmMessages.MESSAGES.containerNotStarted("deploy(Archive<?>)");
        }

        this.server.deployer().deploy(deployment);
        return this;
    }

    /**
     * Provides access to the default ShrinkWrap deployment.
     */
    public Archive<?> createDefaultDeployment() throws Exception {
        if (this.server == null) {
            throw SwarmMessages.MESSAGES.containerNotStarted("createDefaultDeployment()");
        }

        return this.server.deployer().createDefaultDeployment();
    }

    private void createShrinkWrapDomain() {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            if (isFatJar()) {
                Module appModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
                Thread.currentThread().setContextClassLoader(appModule.getClassLoader());
            }
            Domain domain = ShrinkWrap.getDefaultDomain();
            domain.getConfiguration().getExtensionLoader().addOverride(ZipExporter.class, ZipExporterImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(ZipImporter.class, ZipImporterImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(ExplodedExporter.class, ExplodedExporterImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(JavaArchive.class, JavaArchiveImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(WebArchive.class, WebArchiveImpl.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    private static boolean isFatJar() throws IOException {
        URL location = Swarm.class.getProtectionDomain().getCodeSource().getLocation();
        Path root = null;
        if (location.getProtocol().equals("file")) {
            try {
                root = Paths.get(location.toURI());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        } else if (location.toExternalForm().startsWith("jar:file:")) {
            return true;
        }

        if (Files.isRegularFile(root)) {
            try (JarFile jar = new JarFile(root.toFile())) {
                ZipEntry propsEntry = jar.getEntry("META-INF/wildfly-swarm.properties");
                if (propsEntry != null) {
                    try (InputStream in = jar.getInputStream(propsEntry)) {
                        Properties props = new Properties();
                        props.load(in);
                        if (props.containsKey(BootstrapProperties.APP_ARTIFACT)) {
                            System.setProperty(BootstrapProperties.APP_ARTIFACT,
                                               props.getProperty(BootstrapProperties.APP_ARTIFACT));
                        }

                        Set<String> names = props.stringPropertyNames();
                        for (String name : names) {
                            String value = props.getProperty(name);
                            if (System.getProperty(name) == null) {
                                System.setProperty(name, value);
                            }
                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private void loadStageConfiguration(URL url) {
        List<ProjectStage> projectStages = new ProjectStageFactory().loadStages(url);
        String stageName = System.getProperty(SwarmProperties.PROJECT_STAGE, "default");
        ProjectStage stage = null;
        for (ProjectStage projectStage : projectStages) {
            if (projectStage.getName().equals(stageName)) {
                stage = projectStage;
                break;
            }
        }

        if (null == stage)
            throw SwarmMessages.MESSAGES.stageNotFound(stageName);

        System.out.println("[INFO] Using project stage: " + stageName);

        this.stageConfig = Optional.of(stage);
    }

    /**
     * Main entry-point.
     *
     * @param args Ignored.
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
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
        /*
        InputStream in = SwarmConfigurator.class.getClassLoader().getResourceAsStream("wildfly-swarm.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VERSION = props.getProperty("version", "unknown");
        */
        VERSION = "unknown";
    }

    private String[] args;

    private Server server;

    private Set<Class<?>> userComponentClasses = new HashSet<>();

    private List<Fraction> explicitlyInstalledFractions = new ArrayList<>();

    private Optional<ProjectStage> stageConfig = Optional.empty();

    private Optional<URL> xmlConfig = Optional.empty();

    private Optional<URL> stageConfigUrl = Optional.empty();

    private boolean debugBootstrap;
}
