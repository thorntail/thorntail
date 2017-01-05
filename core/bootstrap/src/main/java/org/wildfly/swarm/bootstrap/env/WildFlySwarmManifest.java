package org.wildfly.swarm.bootstrap.env;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmManifest {

    public static final String CLASSPATH_LOCATION = "META-INF/wildfly-swarm-manifest.yaml";

    private static final String ASSET = "asset";

    private static final String MAIN_CLASS = "main-class";

    private static final String HOLLOW = "hollow";

    private static final String PROPERTIES = "properties";

    private static final String MODULES = "modules";

    private static final String BOOTSTRAP_ARTIFACTS = "bootstrap-artifacts";

    private static final String BUNDLE_DEPENDENCIES = "bundle-dependencies";

    private static final String DEPENDENCIES = "dependencies";

    public WildFlySwarmManifest() {

    }

    public WildFlySwarmManifest(URL url) throws IOException {
        read(url);
    }

    public WildFlySwarmManifest(InputStream in) throws IOException {
        read(in);
    }

    public void read(URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            read(in);
        }
    }

    public void read(InputStream in) throws IOException {
        Yaml yaml = new Yaml();
        Map data = (Map) yaml.load(in);

        this.asset = (String) data.get(ASSET);

        this.mainClass = (String) data.get(MAIN_CLASS);
        this.hollow = (boolean) data.get(HOLLOW);

        this.properties.clear();
        this.properties.putAll((Map<?, ?>) data.get(PROPERTIES));

        this.bootstrapModules.clear();
        this.bootstrapModules.addAll((Collection<? extends String>) data.get(MODULES));

        this.bootstrapArtifacts.clear();
        this.bootstrapArtifacts.addAll((Collection<? extends String>) data.get(BOOTSTRAP_ARTIFACTS));

        if (data.get(BUNDLE_DEPENDENCIES) != null) {
            this.bundleDependencies = (boolean) data.get(BUNDLE_DEPENDENCIES);
        }

        this.dependencies.clear();
        this.dependencies.addAll((Collection<? extends String>) data.get(DEPENDENCIES));

        setupProperties();
    }

    public void write(Path path) throws IOException {
        Files.createDirectories(path.getParent());

        try (Writer out = new FileWriter(path.toFile())) {
            out.write(this.toString());
        }
    }

    @Override
    public String toString() {
        Map data = new LinkedHashMap() {{
            if (asset != null) {
                put(ASSET, asset);
            }
            put(MAIN_CLASS, mainClass);
            put(HOLLOW, hollow);
            put(PROPERTIES, properties);
            put(MODULES, bootstrapModules);
            put(BOOTSTRAP_ARTIFACTS, bootstrapArtifacts);
            put(BUNDLE_DEPENDENCIES, bundleDependencies);
            put(DEPENDENCIES, dependencies);
        }};

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        return yaml.dump(data);
    }

    protected void setupProperties() {
        // enumerate all properties, not just those with string
        // values, because Gradle (and others) can set non-string
        // values for things like swarm.http.port (integer)
        Enumeration<?> names = this.properties.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Object value = this.properties.get(name);
            if (value != null) {
                if (System.getProperty(name) == null) {
                    System.setProperty(name, value.toString());
                }
            }
        }

        if (this.bundleDependencies != null && this.bundleDependencies) {
            System.setProperty(BootstrapProperties.BUNDLED_DEPENDENCIES, this.bundleDependencies.toString());
        }
    }

    public void addBootstrapModule(String module) {
        this.bootstrapModules.add(module);
    }

    public List<String> bootstrapModules() {
        return this.bootstrapModules;
    }

    public void addBootstrapArtifact(String artifact) {
        this.bootstrapArtifacts.add(artifact);
    }

    public List<String> bootstrapArtifacts() {
        return this.bootstrapArtifacts;
    }

    public void addDependency(String gav) {
        this.dependencies.add(gav);
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public void setAsset(String asset) {
        if (!this.isHollow()) {
            this.asset = asset;
        }
    }

    public String getAsset() {
        return this.asset;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void bundleDependencies(boolean bundleDependencies) {
        this.bundleDependencies = bundleDependencies;
    }

    public boolean isBundleDependencies() {
        if (this.bundleDependencies == null) {
            return true;
        }
        return this.bundleDependencies;
    }

    public void setMainClass(String mainClass) {
        if (mainClass != null) {
            this.mainClass = mainClass;
        }
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public void setHollow(boolean hollow) {
        this.hollow = hollow;

        if (this.isHollow()) {
            this.asset = null;
        }
    }

    public boolean isHollow() {
        return this.hollow;

    }

    private String asset = null;

    private List<String> bootstrapModules = new ArrayList<>();

    private List<String> bootstrapArtifacts = new ArrayList<>();

    private List<String> dependencies = new ArrayList<>();

    private Properties properties = new Properties();

    private Boolean bundleDependencies;

    private String mainClass = ApplicationEnvironment.DEFAULT_MAIN_CLASS_NAME;

    private boolean hollow;

}

