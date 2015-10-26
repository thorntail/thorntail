/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.cli;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenChecksumPolicy;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepository;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;
import org.wildfly.swarm.arquillian.adapter.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.PackageDetector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class Build {

    public Build() {
        swarmDependencies.add("bootstrap");
        swarmDependencies.add("container");
    }

    public Build source(final File source) {
        this.source = source;

        return this;
    }

    public Build outputDir(final File dir) {
        this.outputDir = dir;

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

    private Map<String, Set<String>> fractionPackages() throws IOException {
        final Properties fractionPackages = new Properties();
        try (InputStream in = Build.class.getResourceAsStream("/org/wildfly/swarm/cli/fraction-packages.properties")) {
            if (in == null) {
                throw new RuntimeException("Failed to load fraction-packages.properties");
            }
            fractionPackages.load(in);
        }

        final Map<String, Set<String>> fractionMap = new HashMap<>();

        for (Map.Entry prop : fractionPackages.entrySet()) {
            Set<String> packages = new HashSet<>();
            packages.addAll(Arrays.asList(((String) prop.getValue()).split(",")));
            fractionMap.put((String)prop.getKey(), packages);
        }

        return fractionMap;
    }

    private Set<String> detectNeededFractions() throws IOException {
        final Map<String, Set<String>> fractionPackages = fractionPackages();

        return PackageDetector
                .detectPackages(new ZipFile(this.source))
                .stream()
                // there's probably a better way to do this
                .map(pkg -> {
                    for (Map.Entry<String, Set<String>> entry : fractionPackages.entrySet()) {
                        if (entry.getValue().contains(pkg)) {

                            return entry.getKey();
                        }
                    }

                    return null;
                })
                .filter(v -> v != null)
                .collect(Collectors.toSet());
    }

    public void run() throws Exception {
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
                .resolveTransitiveDependencies(true);

        this.swarmDependencies.addAll(detectNeededFractions());
       
        for (String dep : this.swarmDependencies) {
            tool.dependency("compile", "org.wildfly.swarm", "wildfly-swarm-" + dep, this.version, "jar", null, null);
        }

        final String jarName = this.name != null ? this.name : baseName;
        final String outDir = this.outputDir.getCanonicalPath();
        System.err.println(String.format("Building %s/%s-swarm.jar with fractions: %s",
                                         outDir,
                                         jarName,
                                         String.join(", ", this.swarmDependencies)));

        tool.build(jarName, Paths.get(outDir));
    }

    private final Set<String> swarmDependencies = new HashSet<>();
    private File source;
    private File outputDir;
    private String name;
    private String version;

}
