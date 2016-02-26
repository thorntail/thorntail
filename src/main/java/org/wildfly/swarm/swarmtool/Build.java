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
package org.wildfly.swarm.swarmtool;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenChecksumPolicy;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepository;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;
import org.wildfly.swarm.arquillian.adapter.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.fractionlist.FractionDescriptor;
import org.wildfly.swarm.fractionlist.FractionList;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.PackageAnalyzer;

public class Build {
    final static Set<String> REQUIRED_FRACTIONS = new HashSet<String>() {{
        add("bootstrap");
        add("container");
    }};

    private final Set<String> swarmDependencies = new HashSet<>();

    private File source;

    private File outputDir;

    private String name;

    private String version;

    private Properties properties;

    private boolean autoDetectFractions = true;

    public Build() {
        swarmDependencies.addAll(REQUIRED_FRACTIONS);
    }

    public Build source(final File source) {
        this.source = source;

        return this;
    }

    public Build outputDir(final File dir) {
        this.outputDir = dir;

        return this;
    }

    public Build properties(final Properties props) {
        this.properties = props;

        return this;
    }

    public Build addSwarmDependencies(final List<String> deps) {
        this.swarmDependencies.addAll(deps);

        return this;
    }

    public Build name(String name) {
        this.name = name;

        return this;
    }

    public Build swarmVersion(String v) {
        this.version = v;

        return this;
    }

    public Build autoDetectFractions(boolean v) {
        this.autoDetectFractions = v;

        return this;
    }

    private Set<String> allRequiredFractions() {
        final FractionList fractionList = FractionList.get();
        final Set<String> fractions = new HashSet<>();

        for (String fractionName : this.swarmDependencies) {
            fractions.add(fractionName);
            FractionDescriptor desc = fractionList.getFractionDescriptor("org.wildfly.swarm", fractionName, Main.VERSION);
            if (desc != null) {
                fractions.addAll(desc.getDependencies()
                        .stream()
                        .map(d -> d.getArtifactId())
                        .collect(Collectors.toSet()));
            }
        }

        return fractions;
    }

    public File run() throws Exception {
        final String[] parts = this.source.getName().split("\\.(?=[^\\.]+$)");
        final String baseName = parts[0];
        final String type = parts[1] == null ? "jar" : parts[1];
        final MavenRemoteRepository jbossPublic =
                MavenRemoteRepositories.createRemoteRepository("jboss-public-repository-group",
                        "http://repository.jboss.org/nexus/content/groups/public/",
                        "default");
        jbossPublic.setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE);
        jbossPublic.setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER);

        final ConfigurableMavenResolverSystem resolver = Maven.configureResolver()
                .withMavenCentralRepo(true)
                .withRemoteRepo(jbossPublic);

        final BuildTool tool = new BuildTool()
                .artifactResolvingHelper(new ShrinkwrapArtifactResolvingHelper(resolver))
                .projectArtifact("", baseName, "", type, this.source)
                .resolveTransitiveDependencies(true)
                .properties(this.properties);

        if (this.autoDetectFractions) {
            this.swarmDependencies.addAll(new PackageAnalyzer(this.source).detectNeededFractions());
        } else {
            System.err.println("Skipping fraction auto-detection");
        }

        for (String dep : this.swarmDependencies) {
            tool.dependency("compile", "org.wildfly.swarm", dep, this.version, "jar", null, null, true);
        }

        final String jarName = this.name != null ? this.name : baseName;
        final String outDir = this.outputDir.getCanonicalPath();
        System.err.println(String.format("Building %s/%s-swarm.jar with fractions: %s",
                outDir,
                jarName,
                String.join(", ",
                        allRequiredFractions()
                                .stream()
                                .sorted()
                                .collect(Collectors.toList()))));

        return tool.build(jarName, Paths.get(outDir));
    }

}
