package org.wildfly.swarm.bootstrap.env;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * @author Bob McWhirter
 */
public class FractionManifest {

    public static final String CLASSPATH_LOCATION = "META-INF/fraction-manifest.yaml";

    private String name;

    private String module;

    private String groupId;

    private String artifactId;

    private String version;

    private int stabilityIndex;

    private String stabilityLevel;

    private List<String> dependencies = new ArrayList<>();

    public FractionManifest() {

    }

    public FractionManifest(URL url) throws IOException {
        read(url);
    }

    public FractionManifest(InputStream in) throws IOException {
        read(in);
    }

    protected void read(URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            read(in);
        }
    }

    protected void read(InputStream in) throws IOException {
        Yaml yaml = new Yaml();
        Map data = (Map) yaml.load(in);

        setName((String) data.get("name"));
        setModule((String) data.get("module"));
        setGroupId((String) data.get("groupId"));
        setArtifactId((String) data.get("artifactId"));
        setVersion((String) data.get("version"));
        setDependencies((Collection<String>) data.get("dependencies"));
        Map stability = (Map) data.get("stability");
        if ( stability != null ) {
            setStabilityIndex((Integer) stability.get("index"));
            setStabilityLevel((String) stability.get("level"));
        }
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getModule() {
        return this.module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStabilityIndex() {
        return stabilityIndex;
    }

    public void setStabilityIndex(int stabilityIndex) {
        this.stabilityIndex = stabilityIndex;
    }

    public String getStabilityLevel() {
        return stabilityLevel;
    }

    public void setStabilityLevel(String stabilityLevel) {
        this.stabilityLevel = stabilityLevel;
    }

    public void setDependencies(Collection<String> dependencies) {
        this.dependencies.clear();
        if ( dependencies != null ) {
            this.dependencies.addAll(dependencies);
        }
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
