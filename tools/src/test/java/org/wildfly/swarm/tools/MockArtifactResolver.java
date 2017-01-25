package org.wildfly.swarm.tools;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Bob McWhirter
 */
public class MockArtifactResolver implements ArtifactResolver, ArtifactResolvingHelper {

    private Map<ArtifactSpec, Entry> entries = new HashMap<>();

    private Map<ArtifactSpec, Archive> artifacts = new HashMap<>();

    private Map<ArtifactSpec, File> resolvedArtifacts = new HashMap<>();


    public void add(ArtifactSpec spec) {
        Archive archive = ShrinkWrap.create(JavaArchive.class);
        archive.add(EmptyAsset.INSTANCE, "nothing");

        Entry entry = new Entry(spec);

        this.entries.put(spec, entry);
        this.artifacts.put(spec, archive);
    }

    public void add(ArtifactSpec spec, Consumer<Entry> config) {
        Archive archive = ShrinkWrap.create(JavaArchive.class);
        archive.add(EmptyAsset.INSTANCE, "nothing");

        Entry entry = new Entry(spec);
        config.accept(entry);

        this.entries.put(spec, entry);
        this.artifacts.put(spec, archive);
    }

    public void add(ArtifactSpec spec, Archive archive, Consumer<Entry> config) {
        Entry entry = new Entry(spec);
        config.accept(entry);

        this.entries.put(spec, entry);
        this.artifacts.put(spec, archive);
    }

    @Override
    public ArtifactSpec resolveArtifact(ArtifactSpec spec) throws Exception {
        return resolve(spec);
    }

    @Override
    public Collection<ArtifactSpec> resolveAllArtifactsTransitively(Collection<ArtifactSpec> specs, boolean excludes) throws Exception {
        return resolveAll(specs, true, excludes);
    }

    @Override
    public Collection<ArtifactSpec> resolveAllArtifactsNonTransitively(Collection<ArtifactSpec> specs) throws Exception {
        return resolveAll(specs, false, false);
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) throws Exception {
        File resolved = resolvedArtifacts.get(spec);

        if (resolved == null) {
            Archive archive = artifacts.get(spec);
            if (archive != null) {
                resolved = File.createTempFile(spec.artifactId(), ".jar");
                resolved.delete();
                resolved.deleteOnExit();
                archive.as(ZipExporter.class).exportTo(resolved);
                this.resolvedArtifacts.put(spec, resolved);
            }
        }
        spec.file = resolved;
        return spec;

    }

    @Override
    public Set<ArtifactSpec> resolveAll(Collection<ArtifactSpec> specs, boolean transitive, boolean defaultExcludes) throws Exception {
        Set<ArtifactSpec> resolved = new HashSet<>();
        for (ArtifactSpec spec : specs) {
            resolved.add(resolve(spec));
            if (transitive) {
                resolved.addAll(resolveAll(this.entries.get(spec).getDependencies().stream().collect(Collectors.toList())));
            }
        }
        return resolved;
    }

    public static class Entry {
        private final ArtifactSpec root;

        private final Set<ArtifactSpec> dependencies = new HashSet<>();

        public Entry(ArtifactSpec root) {
            this.root = root;
        }

        public void addDependency(ArtifactSpec dep) {
            this.dependencies.add(dep);
        }

        public Set<ArtifactSpec> getDependencies() {
            return this.dependencies;
        }

    }
}
