/*
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

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private void validate() {
        assert(this.source != null);
        assert(this.outputDir != null);
        assert(this.source.exists());
        assert(this.source.isFile());
        assert(this.source.canRead());
        assert(this.outputDir.isDirectory());
        assert(this.outputDir.canWrite());
        assert(this.version != null);
    }

    public void run() throws Exception {
        validate();
        final String[] parts = this.source.getName().split("\\.(?=[^\\.]+$)");
        final String baseName = parts[0];
        final String type = parts[1] == null ? "jar" : parts[1];
        final MavenRemoteRepository jbossPublic =
                MavenRemoteRepositories.createRemoteRepository("jboss-public-repository-group",
                                                               "http://repository.jboss.org/nexus/content/groups/public/",
                                                               "default");
        jbossPublic.setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE);
        jbossPublic.setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER);

        ConfigurableMavenResolverSystem resolver = Maven.configureResolver()
                .withMavenCentralRepo(true)
                .withRemoteRepo(jbossPublic);

        BuildTool tool = new BuildTool()
                .artifactResolvingHelper(new ShrinkwrapArtifactResolvingHelper(resolver))
                .projectArtifact("", baseName, "", type, this.source)
                .resolveTransitiveDependencies(true);

        // assume wars are webby so the user doesn't have to specify the undertow fraction every time
        if ("war".equals(type)) {
            this.swarmDependencies.add("undertow");
        }

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
