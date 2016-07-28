package org.wildfly.swarm.container.internal;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
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
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.CommandLineArgs;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.cdi.UnmanagedInstance;
import org.wildfly.swarm.cli.CommandLine;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;

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
        createShrinkWrapDomain();
        determineDeploymentType();

        CommandLine cmd = CommandLine.parse(args);
        cmd.apply(this);

        try {
            String stageFile = System.getProperty(SwarmProperties.PROJECT_STAGE_FILE);
            if (stageFile != null) {
                loadStageConfiguration(new URL(stageFile));
            }

        } catch (MalformedURLException e) {
            System.err.println("[WARN] Failed to parse project stage URL reference, ignoring: " + e.getMessage());
        }
    }

    public List<Fraction> fractions() {
        return this.fractions.values().stream().collect(Collectors.toList());
    }

    public SwarmConfigurator withXmlConfig(URL url) {
        this.xmlConfig = Optional.of(url);
        return this;
    }

    public SwarmConfigurator withStageConfig(URL url) {
        this.stageConfigUrl = Optional.of(url);
        if (null == System.getProperty(SwarmProperties.PROJECT_STAGE_FILE)) {
            loadStageConfiguration(stageConfigUrl.get());
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

    public PostInitContext createPostInitContext() {
        return new PostInitContext();
    }

    public void start(boolean eagerlyOpen) throws Exception {
        if (!this.running) {
            if (stageConfig.isPresent()) {

                System.out.println("[INFO] Starting container with stage config source : " + stageConfigUrl.get());
                this.server.setStageConfig(stageConfig.get());
            }

            if (xmlConfig.isPresent()) {
                System.out.println("[INFO] Starting container with xml config source : " + xmlConfig.get());
                this.server.setXmlConfig(xmlConfig.get());
            }

            this.deployer = this.server.start(eagerlyOpen);
            this.running = true;
        }
    }

    public void stop() throws Exception {
        if (this.running) {
            //TODO Weld may be able to be shutdown once all deployments are complete?
            // Shutdown Weld
            this.weldContainer.shutdown();

            this.server.stop();
            this.running = false;
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

    private void createShrinkWrapDomain() {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            if (isFatJar()) {
                Thread.currentThread().setContextClassLoader(SwarmConfigurator.class.getClassLoader());
                Module appModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
                Thread.currentThread().setContextClassLoader(appModule.getClassLoader());
            }
            this.domain = ShrinkWrap.getDefaultDomain();
            this.domain.getConfiguration().getExtensionLoader().addOverride(ZipExporter.class, ZipExporterImpl.class);
            this.domain.getConfiguration().getExtensionLoader().addOverride(ZipImporter.class, ZipImporterImpl.class);
            this.domain.getConfiguration().getExtensionLoader().addOverride(ExplodedExporter.class, ExplodedExporterImpl.class);
            this.domain.getConfiguration().getExtensionLoader().addOverride(JavaArchive.class, JavaArchiveImpl.class);
            this.domain.getConfiguration().getExtensionLoader().addOverride(WebArchive.class, WebArchiveImpl.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    private static boolean isFatJar() throws IOException {
        URL location = SwarmConfigurator.class.getProtectionDomain().getCodeSource().getLocation();
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

    private void createServer(boolean debugBootstrap) {
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
            t.printStackTrace();
        }
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
            } catch (Exception e) {
                e.printStackTrace();
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
            } catch (Exception e) {
                e.printStackTrace();
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

    private Map<Class<? extends Fraction>, Fraction> fractions = new ConcurrentHashMap<>();

    private List<Fraction> dependentFractions = new ArrayList<>();

    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();

    private Map<String, List<SocketBinding>> socketBindings = new HashMap<>();

    private Map<String, List<OutboundSocketBinding>> outboundSocketBindings = new HashMap<>();

    private List<Interface> interfaces = new ArrayList<>();

    private Server server;

    private Deployer deployer;

    private Domain domain;

    private boolean running = false;

    private Boolean debugBootstrap;

    private String defaultDeploymentType;

    private Optional<ProjectStage> stageConfig = Optional.empty();

    private Optional<URL> xmlConfig = Optional.empty();

    private Optional<URL> stageConfigUrl = Optional.empty();

    private Archive<?> defaultDeployment;

    private URL defaultDeploymentURL;

    private ClassLoader defaultDeploymentClassLoader;

    private WeldContainer weldContainer;

    /**
     * Initialization Context to be passed to Fractions to allow them to provide
     * additional functionality into the Container.
     */
    private class InitContext implements Fraction.InitContext {
        public void fraction(Fraction fraction) {
            SwarmConfigurator.this.dependentFraction(fraction);
        }

        public void socketBinding(SocketBinding binding) {
            socketBinding("default-sockets", binding);
        }

        public void socketBinding(String groupName, SocketBinding binding) {
            SwarmConfigurator.this.socketBinding(groupName, binding);
        }

        public void outboundSocketBinding(OutboundSocketBinding binding) {
            outboundSocketBinding("default-sockets", binding);
        }

        public void outboundSocketBinding(String groupName, OutboundSocketBinding binding) {
            SwarmConfigurator.this.outboundSocketBinding(groupName, binding);
        }

        @Override
        public Optional<StageConfig> projectStage() {
            Optional<StageConfig> cfg = stageConfig.isPresent() ?
                    Optional.of(new StageConfig(stageConfig.get())) : Optional.empty();

            return cfg;
        }
    }

    private class PostInitContext extends InitContext implements Fraction.PostInitContext {
    }
}
