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
package org.wildfly.swarm.container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

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
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.cli.CommandLine;
import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.container.internal.ProjectStageFactory;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.internal.SwarmInternalProperties;

/**
 * A WildFly-Swarm container.
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class Container {

    public static final String VERSION;

    /**
     * Construct a new, un-started container.
     *
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Container() throws Exception {
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
    public Container(boolean debugBootstrap) throws Exception {
        System.setProperty(SwarmInternalProperties.VERSION, VERSION);

        try {
            String stageFile = System.getProperty(SwarmProperties.PROJECT_STAGE_FILE);
            if (stageFile != null) {
                loadStageConfiguration(new URL(stageFile));
            }

        } catch (MalformedURLException e) {
            System.err.println("[WARN] Failed to parse project stage URL reference, ignoring: " + e.getMessage());
        }

        createServer(debugBootstrap);
        createShrinkWrapDomain();
        determineDeploymentType();
    }

    /**
     * Construct a new, un-started container, configured using command-line arguments.
     *
     * @param args The command-line arguments arguments
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Container(String... args) throws Exception {
        this(false, args);
    }

    /**
     * Construct a new, un-started container, configured using command-line arguments.
     *
     * @param debugBootstrap - flag to indicate if the module layer should be put into bootstrap debug mode. Same as
     *                       the jboss-module -debuglog mode which enables trace logging to System.out during the
     *                       initial bootstrap of the module layer.
     * @param args The command-line arguments arguments
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Container(boolean debugBootstrap, String... args) throws Exception {
        this(debugBootstrap);
        CommandLine cmd = CommandLine.parse(args);
        cmd.apply(this);
    }

    public Container withXmlConfig(URL url) {
        this.xmlConfig = Optional.of(url);
        return this;
    }

    public Container withStageConfig(URL url) {
        if (null == System.getProperty(SwarmProperties.PROJECT_STAGE_FILE)) {
            loadStageConfiguration(url);
        } else {
            System.out.println("[INFO] Project stage superseded by external configuration " + System.getProperty(SwarmProperties.PROJECT_STAGE_FILE));
        }

        return this;
    }

    public StageConfig stageConfig() {
        if (!stageConfig.isPresent())
            throw new RuntimeException("Stage config is not present");
        return new StageConfig(stageConfig.get());
    }

    public boolean hasStageConfig() {
        return stageConfig.isPresent();
    }

    public static boolean isFatJar() throws IOException {
        URL location = Container.class.getProtectionDomain().getCodeSource().getLocation();
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

    public void applyFractionDefaults(Server server) throws Exception {
        Set<Class<? extends Fraction>> availFractions = server.getFractionTypes();

        // Process any dependent fractions from Application added fractions
        if (!this.dependentFractions.isEmpty()) {
            this.dependentFractions.stream()
                    .filter(dependentFraction -> this.fractions.get(dependentFraction.getClass()) == null)
                    .forEach(this::fraction);
            this.dependentFractions.clear();
        }

        // Provide defaults for those remaining
        availFractions.stream()
                .filter(fractionClass -> this.fractions.get(fractionClass) == null)
                .forEach(fractionClass -> fractionDefault(server.createDefaultFor(fractionClass)));

        // Determine if any dependent fractions should override non Application added fractions
        if (!this.dependentFractions.isEmpty()) {
            this.dependentFractions.stream()
                    .filter(dependentFraction ->
                            this.fractions.get(dependentFraction.getClass()) == null
                                    || (this.fractions.get(dependentFraction.getClass()) != null
                                    && this.defaultFractionTypes.contains(dependentFraction.getClass())))
                    .forEach(this::fraction);
            this.dependentFractions.clear();
        }
    }

    /**
     * Add a fraction to the container.
     *
     * @param fraction The fraction to add.
     * @return The container.
     */
    public Container fraction(Fraction fraction) {
        if (fraction != null) {
            this.fractions.put(fractionRoot(fraction.getClass()), fraction);
            this.fractionsBySimpleName.put(fraction.simpleName(), fraction);
            fraction.initialize(new InitContext());
        }
        return this;
    }

    public Container fraction(Supplier<Fraction> supplier) {
        return fraction(supplier.get());
    }

    public List<Fraction> fractions() {
        return this.fractions.values().stream().collect(Collectors.toList());
    }

    /**
     * Configure a network interface.
     *
     * @param name       The name of the interface.
     * @param expression The expression to define the interface.
     * @return The container.
     */
    public Container iface(String name, String expression) {
        this.interfaces.add(new Interface(name, expression));
        return this;
    }

    public List<Interface> ifaces() {
        return this.interfaces;
    }

    /**
     * Configure a socket-binding-group.
     *
     * @param group The socket-binding group to add.
     * @return The container.
     */
    public Container socketBindingGroup(SocketBindingGroup group) {
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

    /**
     * Start the container.
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container start() throws Exception {
        return start(false);
    }

    public Container start(boolean eagerlyOpen) throws Exception {
        if (!this.running) {

            setupXmlConfig();
            setupStageConfig();

            this.deployer = this.server.start(this, eagerlyOpen);
            this.running = true;
        }

        return this;
    }

    private void setupStageConfig() throws Exception {
        ProjectStage projectStage = stageConfig.isPresent() ? stageConfig.get() : null;

        // auto discover META-INF/project-stages.yml in default deployment
        if (projectStage == null && System.getProperty(SwarmProperties.PROJECT_STAGE_FILE) == null) {
            String resourcePath = "META-INF/project-stages.yml";
            ClassLoader classLoader = getDefaultDeploymentClassLoader();
            URL configURL = classLoader.getResource(resourcePath);
            if (configURL != null) {
                projectStage = loadStageConfiguration(configURL);
            }
        }

        if (projectStage != null) {
            this.server.setStageConfig(projectStage);
        }
    }

    private void setupXmlConfig() throws Exception {
        URL configURL = xmlConfig.isPresent() ? xmlConfig.get() : null;

        // auto discover META-INF/standalone.xml in default deployment
        if (configURL == null) {
            String resourcePath = "META-INF/standalone.xml";
            ClassLoader classLoader = getDefaultDeploymentClassLoader();
            configURL = classLoader.getResource(resourcePath);
        }

        if (configURL != null) {
            this.server.setXmlConfig(configURL);
        }
    }

    /**
     * Stop the container, undeploying all deployments.
     *
     * @return THe container.
     * @throws Exception If an error occurs.
     */
    public Container stop() throws Exception {
        if (this.running) {
            this.server.stop();
            this.running = false;
        }

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
    public Container start(Archive<?> deployment) throws Exception {
        return start().deploy(deployment);
    }

    /**
     * Deploy the default WAR deployment.
     * <p/>
     * <p>For WAR-based applications, the primary WAR artifact iwll be deployed.</p>
     *
     * @return The container.
     * @throws DeploymentException if an error occurs.
     */
    public Container deploy() throws DeploymentException {
        Archive<?> deployment = createDefaultDeployment();
        if (deployment == null) {
            throw new DeploymentException("Unable to create default deployment");
        } else {
            return deploy(deployment);
        }
    }

    /**
     * Deploy an archive.
     *
     * @param deployment The ShrinkWrap archive to deploy.
     * @return The container.
     * @throws DeploymentException if an error occurs.
     */
    public Container deploy(Archive<?> deployment) throws DeploymentException {
        if (!this.running) {
            throw new RuntimeException("The Container has not been started.");
        }

        this.deployer.deploy(deployment);

        return this;
    }

    /**
     * Get the possibly null container main method arguments.
     *
     * @return main method arguments, possibly null
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Set the main method arguments. This will be available as a ValueService<String[]> under the name
     * wildfly.swarm.main-args
     *
     * @param args arguments passed to the main(String[]) method.
     */
    public void setArgs(String[] args) {
        this.args = args;
    }

    public PostInitContext createPostInitContext() {
        return new PostInitContext();
    }

    /**
     * Provides access to the default ShrinkWrap deployment.
     */
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

    /**
     * @return The default deployment or null
     */
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

    private void createShrinkWrapDomain() throws ModuleLoadException {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            if (isFatJar()) {
                Thread.currentThread().setContextClassLoader(Container.class.getClassLoader());
                Module appModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
                Thread.currentThread().setContextClassLoader(appModule.getClassLoader());
            }
            this.domain = ShrinkWrap.getDefaultDomain();
            this.domain.getConfiguration().getExtensionLoader().addOverride(ZipExporter.class, ZipExporterImpl.class);
            this.domain.getConfiguration().getExtensionLoader().addOverride(ZipImporter.class, ZipImporterImpl.class);
            this.domain.getConfiguration().getExtensionLoader().addOverride(ExplodedExporter.class, ExplodedExporterImpl.class);
            this.domain.getConfiguration().getExtensionLoader().addOverride(JavaArchive.class, JavaArchiveImpl.class);
            this.domain.getConfiguration().getExtensionLoader().addOverride(WebArchive.class, WebArchiveImpl.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    private void createServer(boolean debugBootstrap) throws Exception {
        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", BootModuleLoader.class.getName());
        }
        if (debugBootstrap) {
            Module.setModuleLogger(new StreamModuleLogger(System.err));
        }
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.container", "runtime"));
        Class<?> serverClass = module.getClassLoader().loadClass("org.wildfly.swarm.container.runtime.RuntimeServer");
        try {
            this.server = (Server) serverClass.newInstance();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void fractionDefault(Fraction defaultFraction) {
        if (defaultFraction == null) {
            return;
        }
        this.defaultFractionTypes.add(fractionRoot(defaultFraction.getClass()));
        fraction(defaultFraction);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Fraction> fractionRoot(Class<? extends Fraction> fractionClass) {
        Class<? extends Fraction> fractionRoot = fractionClass;
        boolean rootFound = false;

        while (!rootFound) {
            Class<?>[] interfaces = fractionRoot.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (anInterface.getName().equals(Fraction.class.getName())) {
                    rootFound = true;
                    break;
                }
            }

            if (!rootFound) {
                fractionRoot = (Class<? extends Fraction>) fractionRoot.getSuperclass();
            }
        }

        return fractionRoot;
    }

    /**
     * Add a fraction to the container that is a dependency of another fraction.
     *
     * @param fraction The dependent fraction to add.
     */
    private void dependentFraction(Fraction fraction) {
        this.dependentFractions.add(fraction);
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

    protected String determineDeploymentType() throws IOException {
        if (this.defaultDeploymentType == null) {
            this.defaultDeploymentType = determineDeploymentTypeInternal();
            System.setProperty(BootstrapProperties.DEFAULT_DEPLOYMENT_TYPE, this.defaultDeploymentType);
        }
        return this.defaultDeploymentType;
    }

    protected String determineDeploymentTypeInternal() throws IOException {
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

        if (Files.exists(Paths.get("pom.xml"))) {
            try (BufferedReader in = new BufferedReader(new FileReader(Paths.get("pom.xml").toFile()))) {
                String line;

                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("<packaging>jar</packaging>")) {
                        return "jar";
                    } else if (line.equals("<packaging>war</packaging>")) {
                        return "war";
                    }
                }
            }
        }

        if (Files.exists(Paths.get("Mavenfile"))) {
            try (BufferedReader in = new BufferedReader(new FileReader(Paths.get("Mavenfile").toFile()))) {
                String line;

                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("packaging :jar")) {
                        return "jar";
                    } else if (line.equals("packaging :war")) {
                        return "war";
                    }
                }
            }
        }

        // when in doubt, assume at least a .jar
        return "jar";
    }

    private ProjectStage loadStageConfiguration(URL url) {
        try {
            return enableStageConfiguration(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load stage configuration from URL :" + url.toExternalForm(), e);
        }
    }

    private ProjectStage enableStageConfiguration(InputStream input) {
        List<ProjectStage> projectStages = new ProjectStageFactory().loadStages(input);
        String stageName = System.getProperty(SwarmProperties.PROJECT_STAGE, "default");
        ProjectStage stage = null;
        for (ProjectStage projectStage : projectStages) {
            if (projectStage.getName().equals(stageName)) {
                stage = projectStage;
                break;
            }
        }

        if (null == stage)
            throw new RuntimeException("Project stage '" + stageName + "' cannot be found");

        System.out.println("[INFO] Using project stage: " + stageName);

        this.stageConfig = Optional.of(stage);
        return stage;
    }

    static {
        InputStream in = Container.class.getClassLoader().getResourceAsStream("wildfly-swarm.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VERSION = props.getProperty("version", "unknown");
    }


    private Map<Class<? extends Fraction>, Fraction> fractions = new ConcurrentHashMap<>();

    private Map<String, Fraction> fractionsBySimpleName = new ConcurrentHashMap<>();

    private List<Fraction> dependentFractions = new ArrayList<>();

    private Set<Class<? extends Fraction>> defaultFractionTypes = new HashSet<>();

    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();

    private Map<String, List<SocketBinding>> socketBindings = new HashMap<>();

    private Map<String, List<OutboundSocketBinding>> outboundSocketBindings = new HashMap<>();

    private List<Interface> interfaces = new ArrayList<>();

    private Server server;

    private Deployer deployer;

    private Domain domain;

    private boolean running = false;

    private String defaultDeploymentType;

    private String[] args;

    private Optional<ProjectStage> stageConfig = Optional.empty();

    private Optional<URL> xmlConfig = Optional.empty();

    private Archive<?> defaultDeployment;

    private URL defaultDeploymentURL;

    private ClassLoader defaultDeploymentClassLoader;

    /**
     * Initialization Context to be passed to Fractions to allow them to provide
     * additional functionality into the Container.
     */
    private class InitContext implements Fraction.InitContext {
        public void fraction(Fraction fraction) {
            Container.this.dependentFraction(fraction);
        }

        public void socketBinding(SocketBinding binding) {
            socketBinding("default-sockets", binding);
        }

        public void socketBinding(String groupName, SocketBinding binding) {
            Container.this.socketBinding(groupName, binding);
        }

        public void outboundSocketBinding(OutboundSocketBinding binding) {
            outboundSocketBinding("default-sockets", binding);
        }

        public void outboundSocketBinding(String groupName, OutboundSocketBinding binding) {
            Container.this.outboundSocketBinding(groupName, binding);
        }

        @Override
        public Optional<StageConfig> projectStage() {
            Optional<StageConfig> cfg = stageConfig.isPresent() ?
                    Optional.of(new StageConfig(stageConfig.get())) : Optional.empty();

            return cfg;
        }
    }

    private class PostInitContext extends InitContext implements Fraction.PostInitContext {
        public boolean hasFraction(String simpleName) {
            return fractions().stream().anyMatch((f) -> f.simpleName().equalsIgnoreCase(simpleName));
        }

        public Fraction fraction(String simpleName) {
            Optional<Fraction> opt = fractions().stream().filter((f) -> f.simpleName().equalsIgnoreCase(simpleName)).findFirst();
            return opt.orElse(null);
        }

    }
}
