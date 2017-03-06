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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.enterprise.inject.Vetoed;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.log.StreamModuleLogger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.ExplodedExporterImpl;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.jboss.shrinkwrap.impl.base.importer.ExplodedImporterImpl;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.logging.BackingLoggerManager;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.cli.CommandLine;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.config.ConfigViewFactory;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.internal.ServerBootstrap;
import org.wildfly.swarm.container.internal.WeldShutdown;
import org.wildfly.swarm.internal.OutboundSocketBindingRequest;
import org.wildfly.swarm.internal.SocketBindingRequest;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.config.ConfigView;

/**
 * Default {@code main(...)} if an application does not provide one.
 *
 * <p>This simply constructs a default container, starts it and performs
 * a default deployment.  Typically only useful for barren WAR applications.</p>
 *
 * <p>If providing their own {@code main(...)}, then the following needs to be known:</p>
 *
 * <ul>
 * <li>Any usage of {@code java.util.logging} may only follow the initial constructor
 * of {@code new Swarm()}.</li>
 * <li>While this object may appear to be thread-safe, it does rely on general
 * static instances for some facilities.  Therefore, it should not be instantiated
 * several times concurrently.</li>
 * <li>It can be instantiated multiple times <b>serially</b>, as long as one instance
 * is disposed before another is created and used.  This limitation may be removed
 * in a future version, if required.</li>
 * </ul>
 *
 * <p>If using this class either directly or implicit as a {@code main(...)}, certain
 * command-line facilities are available.  If used directly, the user should pass the
 * {@code String...args} from his own {@code main(...)} to the constructor of this
 * class if these command-line facilities are desired.</p>
 *
 * <p>Many internal aspects of the runtime container may be configured using the Java
 * APIs for various fractions, XML configuration files, YAML configuration files, and
 * Java system properties.</p>
 *
 * <p>Configuration ordering works as follows: Fractions configured through an XML
 * configuration file takes precedence over the same fraction configured through the
 * Java API.  YAML or system properties may override portions or attributes of fractions
 * defined either way.  A system property override binds more strongly than YAML configuration.</p>
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Vetoed
public class Swarm {

    private static final String BOOT_MODULE_PROPERTY = "boot.module.loader";

    public static final String APPLICATION_MODULE_NAME = "swarm.application";

    private static final String CONTAINER_MODULE_NAME = "swarm.container";

    private static final String PROJECT_STAGES_FILE = "project-stages.yml";

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
        this(debugBootstrap, null, null, args);
    }

    public Swarm(Properties properties, String... args) throws Exception {
        this(false, properties, null, args);
    }

    public Swarm(boolean debug, Properties properties, String... args) throws Exception {
        this(debug, properties, null, args);
    }

    public Swarm(Properties properties, Map<String, String> environment, String... args) throws Exception {
        this(false, properties, environment, args);
    }

    public Swarm(boolean debugBootstrap, Properties properties, Map<String, String> environment, String... args) throws Exception {
        if (System.getProperty(BOOT_MODULE_PROPERTY) == null) {
            System.setProperty(BOOT_MODULE_PROPERTY, BootModuleLoader.class.getName());
        }
        if (debugBootstrap) {
            Module.setModuleLogger(new StreamModuleLogger(System.err));
        }

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
                Class<?> logManagerClass = loggingModule.getClassLoader().loadClass("org.wildfly.swarm.container.runtime.logging.JBossLoggingManager");
                BootstrapLogger.setBackingLoggerManager((BackingLoggerManager) logManagerClass.newInstance());
            } finally {
                Thread.currentThread().setContextClassLoader(originalCl);
            }
        } catch (ModuleLoadException e) {
            System.err.println("[WARN] logging not available, logging will not be configured");
        }
        installModuleMBeanServer();
        createShrinkWrapDomain();

        this.configView = ConfigViewFactory.defaultFactory(properties, environment);
        this.commandLine = CommandLine.parse(args);
        this.commandLine.apply(this);
        initializeConfigView();

        this.isConstructing = false;
    }

    /**
     * Retrieve the parsed command-line from this instance.
     *
     * <p>This method is only applicable if the {@code String...args} was passed through
     * the constructor or {@link #setArgs(String...)} was called to provide the command-line
     * arguments.</p>
     *
     * @return The parsed command-line.
     */
    public CommandLine getCommandLine() {
        return this.commandLine;
    }

    /**
     * Pass the effective command-line arguments to this instance.
     *
     * @param args The arguments.
     */
    public void setArgs(String... args) {
        this.args = args;
    }

    /**
     * Specify an XML configuration file (in usual WildFly {@code standalone.xml}) format.
     *
     * <p>Usage of an XML configuration file is <b>not</b> exclusive with other configuration
     * methods.</p>
     *
     * @param url The URL of the XML configuration file.
     * @return This instance.
     * @see #withConfig(URL)
     */
    public Swarm withXmlConfig(URL url) {
        this.xmlConfig = Optional.of(url);
        return this;
    }

    public Swarm withConfig(URL url) throws IOException {
        if (!isConstructing) {
            String uuid = UUID.randomUUID().toString();
            this.configView.load(uuid, url);
            this.configView.withProfile(uuid);
        }
        this.configs.add(url);
        return this;
    }

    public Swarm withProfile(String name) {
        if (!isConstructing) {
            this.configView.load(name);
            this.configView.withProfile(name);
        }
        this.profiles.add(name);
        return this;
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
     * Add an outbound socket-binding to the container.
     *
     * <p>In the event the specified {@code socketBindingGroup} does not exist, the socket-binding
     * will be completely ignored.</p>
     *
     * TODO fix the above-mentioned issue.
     *
     * @param socketBindingGroup The name of the socket-binding group to attach a binding to.
     * @param binding            The outbound socket-binding to add.
     * @return This container.
     */
    public Swarm outboundSocketBinding(String socketBindingGroup, OutboundSocketBinding binding) {
        this.outboundSocketBindings.add(new OutboundSocketBindingRequest(socketBindingGroup, binding));
        return this;
    }

    /**
     * Add an inbound socket-binding to the container.
     *
     * <p>In the even the specified {@code socketBindingGroup} does no exist, the socket-binding
     * will be completely ignored.</p>
     *
     * TODO fix the above-mentioned issue.
     *
     * @param socketBindingGroup The name of the socket-binding group to attach a binding to.
     * @param binding            The inbound socket-binding to add.
     * @return This container.
     */
    public Swarm socketBinding(String socketBindingGroup, SocketBinding binding) {
        this.socketBindings.add(new SocketBindingRequest(socketBindingGroup, binding));
        return this;
    }

    /**
     * Start the container.
     *
     * <p>This is a blocking call, which guarateens that when it returns without error, the
     * container is fully started.</p>
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Swarm start() throws Exception {
        try (AutoCloseable handle = Performance.time("Swarm.start()")) {

            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(CONTAINER_MODULE_NAME));
            Class<?> bootstrapClass = module.getClassLoader().loadClass("org.wildfly.swarm.container.runtime.ServerBootstrapImpl");

            ServerBootstrap bootstrap = (ServerBootstrap) bootstrapClass.newInstance();
            bootstrap
                    .withArguments(this.args)
                    .withBootstrapDebug(this.debugBootstrap)
                    .withExplicitlyInstalledFractions(this.explicitlyInstalledFractions)
                    .withSocketBindings(this.socketBindings)
                    .withOutboundSocketBindings(this.outboundSocketBindings)
                    .withUserComponents(this.userComponentClasses)
                    .withXmlConfig(this.xmlConfig)
                    .withConfigView(this.configView.get(true));

            this.server = bootstrap.bootstrap();

            return this;
        }
    }

    /**
     * Start the container with a deployment.
     *
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
     * Stop the container, first undeploying all deployments.
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

        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(CONTAINER_MODULE_NAME));
        Class<?> shutdownClass = module.getClassLoader().loadClass("org.wildfly.swarm.container.runtime.WeldShutdownImpl");

        WeldShutdown shutdown = (WeldShutdown) shutdownClass.newInstance();
        shutdown.shutdown();

        return this;
    }

    /**
     * Perform a default deployment.
     *
     * <p>For regular uberjars, it is effectively a short-cut for {@code deploy(swarm.createDefaultDeployment())},
     * deploying the baked-in deployment.</p>
     *
     * <p>For hollow uberjars, it deploys whatever deployments were passed through the command-line, as
     * none are baked-in.</p>
     *
     * @return The container.
     * @throws DeploymentException   if an error occurs.
     * @throws IllegalStateException if the container has not already been started.
     * @see #Swarm(String...)
     * @see #setArgs(String...)
     * @see #deploy(Archive)
     * @see #createDefaultDeployment()
     */
    public Swarm deploy() throws IllegalStateException, DeploymentException {
        if (this.server == null) {
            throw SwarmMessages.MESSAGES.containerNotStarted("deploy()");
        }

        if (ApplicationEnvironment.get().isHollow()) {
            this.server.deployer().deploy(
                    getCommandLine().extraArguments()
                            .stream()
                            .map(e -> Paths.get(e))
                            .collect(Collectors.toList())
            );
        } else {
            this.server.deployer().deploy();
        }
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
     * Retrieve the default ShrinkWrap deployment.
     *
     * @return The default deployment, unmodified.
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
                Module appModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(APPLICATION_MODULE_NAME));
                Thread.currentThread().setContextClassLoader(appModule.getClassLoader());
            }
            Domain domain = ShrinkWrap.getDefaultDomain();
            domain.getConfiguration().getExtensionLoader().addOverride(ZipExporter.class, ZipExporterImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(ZipImporter.class, ZipImporterImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(ExplodedExporter.class, ExplodedExporterImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(ExplodedImporter.class, ExplodedImporterImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(JavaArchive.class, JavaArchiveImpl.class);
            domain.getConfiguration().getExtensionLoader().addOverride(WebArchive.class, WebArchiveImpl.class);
        } catch (Exception e) {
            SwarmMessages.MESSAGES.shrinkwrapDomainSetupFailed(e);
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

    private void initializeConfigView() throws IOException, ModuleLoadException {
        try (AutoCloseable handle = Performance.time("Loading YAML")) {
            if (System.getProperty(SwarmProperties.PROJECT_STAGE_FILE) != null) {
                String file = System.getProperty(SwarmProperties.PROJECT_STAGE_FILE);
                boolean loaded = false;
                try {
                    Path path = Paths.get(file);
                    if (Files.exists(path)) {
                        this.configView.load("stages", path.toUri().toURL());
                        loaded = true;
                    }
                } catch (InvalidPathException e) {
                    // ignore
                }
                if (!loaded) {
                    // try it as a URL
                    try {
                        URL url = new URL(file);
                        this.configView.load("stages", url);
                    } catch (MalformedURLException e) {
                        // oh well
                    }
                }
            }

            //List<String> activatedNames = new ArrayList<>();

            if (System.getProperty(SwarmProperties.PROJECT_STAGE) != null) {
                String prop = System.getProperty(SwarmProperties.PROJECT_STAGE);
                String[] activated = prop.split(",");
                for (String each : activated) {
                    this.configView.withProfile(each);
                }
            }

            int counter = 0;
            for (URL config : this.configs) {
                String syntheticName = "cli-" + (++counter);
                this.configView.load(syntheticName, config);
                this.configView.withProfile(syntheticName);
            }

            // deprecated project-stages.yml
            this.configView.load("stages");
            this.configView.load("defaults");

            for (String profile : this.profiles) {
                this.configView.load(profile);
                this.configView.withProfile(profile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Main entry-point if a user does not specify a custom {@code main(...)}-containing class.
     *
     * <p>The default behaviour of this {@code main(...)} is to start the container entirely
     * with defaults and deploy the default deployment.</p>
     *
     * @param args The command-line arguments from the invocation.
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
        if (System.getProperty(BOOT_MODULE_PROPERTY) == null) {
            System.setProperty(BOOT_MODULE_PROPERTY, "org.wildfly.swarm.bootstrap.modules.BootModuleLoader");
        }

        Swarm swarm = new Swarm(args);
        swarm.start().deploy();
    }

    private static ArtifactLookup artifactLookup() {
        return ArtifactLookup.get();
    }

    /**
     * Retrieve an artifact that was part of the original build using a
     * full or simplified Maven GAV specifier.
     *
     * <p>The following formats of GAVs are supported:</p>
     *
     * <ul>
     * <li>groupId:artifactId</li>
     * <li>groupId:artifactId:version</li>
     * <li>groupId:artifactId:packaging:version</li>
     * <li>groupId:artifactId:packaging:version:classifier</li>
     * </ul>
     *
     * <p>Only artifacts that were compiled with the user's project with
     * a scope of {@code compile} are available through lookup.</p>
     *
     * <p>In the variants that include a {@code version} parameter, it may be
     * replaced by a literal asterisk in order to avoid hard-coding versions
     * into the application.</p>
     *
     * @param gav The Maven GAV.
     * @return The located artifact, as a {@code JavaArchive}.
     * @throws Exception If the specified artifact is not locatable.
     */
    public static JavaArchive artifact(String gav) throws Exception {
        return artifactLookup().artifact(gav);
    }

    /**
     * Retrieve an artifact that was part of the original build using a
     * full or simplified Maven GAV specifier, returning an archive with a
     * specified name.
     *
     * @param gav The Maven GAV.
     * @return The located artifact, as a {@code JavaArchive} with the specified name.
     * @throws Exception If the specified artifact is not locatable.
     * @see #artifact(String)
     */
    public static JavaArchive artifact(String gav, String asName) throws Exception {
        return artifactLookup().artifact(gav, asName);
    }

    /**
     * Retrieve all dependency artifacts for the user's project.
     *
     * @return All dependencies, as {@code JavaArchive} objects.
     * @throws Exception
     */
    public static List<JavaArchive> allArtifacts() throws Exception {
        return artifactLookup().allArtifacts();
    }

    /**
     * Installs the Module MBeanServer.
     */
    private void installModuleMBeanServer() {
        try {
            Method method = ModuleLoader.class.getDeclaredMethod("installMBeanServer");
            method.setAccessible(true);
            method.invoke(null);
        } catch (Exception e) {
            SwarmMessages.MESSAGES.moduleMBeanServerNotInstalled(e);
        }
    }

    /**
     * Retrieve the configuration view.
     *
     * @return The configuration view.
     */
    public ConfigView configView() {
        return this.configView.get();
    }

    /**
     * Retrieve the configuration view in a deprecated manner.
     *
     * @return The {@code ConfigView} through a deprecated interface.
     * @see #configView()
     */
    @Deprecated
    public StageConfig stageConfig() {
        return this.configView.get();
    }

    private String[] args;

    private Server server;

    private Set<Class<?>> userComponentClasses = new HashSet<>();

    private List<SocketBindingRequest> socketBindings = new ArrayList<>();

    private List<OutboundSocketBindingRequest> outboundSocketBindings = new ArrayList<>();

    private List<Fraction> explicitlyInstalledFractions = new ArrayList<>();

    private ConfigViewFactory configView;

    private Optional<URL> xmlConfig = Optional.empty();

    private List<URL> configs = new ArrayList<>();

    private List<String> profiles = new ArrayList<>();

    private boolean debugBootstrap;

    private boolean isConstructing = true;

}
