/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.plugin.maven;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 *         <br>
 *         Date: 5/12/17
 */
public class ExtraArtifactsHandler {

    /**
     * Takes a list of dependencies and creates a list of extras (poms, javadocs and/or sources) for those dependencies.
     * <p/>
     * Set one or more of the following system properties to make the method return artifacts of a specified type:
     * <ul>
     * <li><code>thorntail.download.sources</code> for sources</li>
     * <li><code>thorntail.download.poms</code> for pom files</li>
     * <li><code>thorntail.download.javadocs</code> for javadocs</li>
     * </ul>
     * <p/>
     *
     * @param nodes list of dependencies
     * @return list of extra artifacts
     */
    public static List<DependencyNode> getExtraDependencies(List<DependencyNode> nodes) {
        ExtraArtifactsHandler fetcher = new ExtraArtifactsHandler(nodes);

        if (isSet("thorntail.download.sources")) {
            fetcher.addWithClassifier("sources");
        }

        if (isSet("thorntail.download.poms")) {
            fetcher.addWithExtension("pom");
        }

        if (isSet("thorntail.download.javadocs")) {
            fetcher.addWithClassifier("javadoc");
        }

        return fetcher.output;
    }

    private static boolean isSet(String key) {
        String value = System.getProperty(key);
        return value != null && !"false".equals(value);
    }

    public void addWithExtension(String extension) {
        addDependencies(
                a -> a.getExtension().equals(extension) && StringUtils.isEmpty(a.getClassifier()),
                Optional.of(extension), Optional.empty()
        );
    }

    public void addWithClassifier(String classifier) {
        addDependencies(
                a -> a.getClassifier().equals(classifier) && "jar".equals(a.getExtension()),
                Optional.empty(), Optional.of(classifier)
        );
    }

    private void addDependencies(Function<Artifact, Boolean> duplicateFilter, Optional<String> extension, Optional<String> classifier) {
        List<Dependency> dependencies = input.stream()
                .map(DependencyNode::getDependency)
                .collect(Collectors.toList());

        Set<String> existingGavs = dependencies.stream()
                .map(Dependency::getArtifact)
                .filter(duplicateFilter::apply)
                .map(this::toGav)
                .collect(Collectors.toSet());

        List<DependencyNode> newNodes = input.stream()
                .filter(n -> !existingGavs.contains(toGav(n.getDependency().getArtifact())))
                .map(n -> createNode(n, extension, classifier))
                .collect(Collectors.toList());
        output.addAll(newNodes);
    }

    private DependencyNode createNode(DependencyNode n, Optional<String> extension, Optional<String> classifier) {
        Artifact original = n.getArtifact();
        Artifact withExtension =
                new DefaultArtifact(original.getGroupId(),
                        original.getArtifactId(),
                        classifier.orElse(original.getClassifier()),
                        extension.orElse(original.getExtension()),
                        original.getVersion(),
                        original.getProperties(),
                        (File) null);

        DefaultDependencyNode nodeWithClassifier = new DefaultDependencyNode(new Dependency(withExtension, "system"));

        return nodeWithClassifier;
    }

    private String toGav(Artifact artifact) {
        return String.format("%s:%s:%s",
                artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    }

    public ExtraArtifactsHandler(List<DependencyNode> nodes) {
        this.input = nodes;
    }

    private final List<DependencyNode> input;
    private final List<DependencyNode> output = new ArrayList<>();
}
