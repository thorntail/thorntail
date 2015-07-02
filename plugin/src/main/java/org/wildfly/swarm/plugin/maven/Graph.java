package org.wildfly.swarm.plugin.maven;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class Graph {

    public class Module {

        private final String module;
        private final String slot;

        private Set<Artifact> artifacts = new HashSet<>();
        private Set<Module> dependencies = new HashSet<>();
        private Set<Module> dependents = new HashSet<>();

        public Module(String module, String slot) {
            this.module = module;
            this.slot = slot;
        }

        public String getID() {
            return this.module + ":" + this.slot;
        }

        public void addArtifact(Artifact artifact) {
            this.artifacts.add(artifact);
            artifact.addDependent(this);
        }

        public void addDependency(Module module) {
            this.dependencies.add(module);
            module.addDependent(this);
        }

        void addDependent(Module module) {
            this.dependents.add(module);
        }

        public Set<Module> getDependents() {
            return this.dependents;
        }

        public void accept(GraphVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class Artifact {

        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String classifier;

        private Set<Module> dependents = new HashSet<>();

        public Artifact(String groupId, String artifactId, String version, String classifier) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.classifier = classifier;
        }

        public String getGAV() {
            String gav = this.groupId + ":" + this.artifactId + ":" + this.version;
            if (this.classifier != null) {
                gav = gav + ":" + this.classifier;
            }

            return gav;
        }

        void addDependent(Module module) {
            this.dependents.add(module);
        }

        public Set<Module> getDependents() {
            return this.dependents;
        }

        public void accept(GraphVisitor visitor) {
            visitor.visit(this);
        }
    }

    private Map<String, Module> modules = new HashMap<>();
    private Map<String, Artifact> artifacts = new HashMap<>();

    public Graph() {

    }

    public void accept(GraphVisitor visitor) {
        visitor.visit(this);
    }

    public Artifact getClosestArtifact(String gav) {

        Set<String> keys = this.artifacts.keySet();
        for (String each : keys) {
            if (each.startsWith(gav)) {
                return this.artifacts.get(each);
            }
        }

        return null;
    }

    public Artifact getArtifact(String groupId, String artifactId, String version, String classifier) {
        String key = groupId + ":" + artifactId + ":" + version;
        if (classifier != null) {
            key = key + ":" + classifier;
        }

        Artifact a = this.artifacts.get(key);
        if (a == null) {
            a = new Artifact(groupId, artifactId, version, classifier);
            this.artifacts.put(key, a);
        }

        return a;
    }

    public Module getModule(String module, String slot) {

        Module m = this.modules.get(module + ":" + slot);
        if (m == null) {
            m = new Module(module, slot);
            this.modules.put(module + ":" + slot, m);
        }

        return m;

    }


}
