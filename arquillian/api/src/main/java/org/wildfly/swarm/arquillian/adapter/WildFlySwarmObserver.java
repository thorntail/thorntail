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
package org.wildfly.swarm.arquillian.adapter;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.wildfly.swarm.arquillian.ArtifactDependencies;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmObserver {

    @SuppressWarnings("unused")
    public void afterSetup(@Observes final AfterSetup event) throws Exception {
        this.container = (WildFlySwarmContainer) event.getDeployableContainer();
    }

    @SuppressWarnings({"unused", "unchecked"})
    public void generate(@Observes(precedence = 100) final GenerateDeployment event) throws Exception {
        final Class testClass = event.getTestClass().getJavaClass();
        this.container.setTestClass(testClass);

        final List<Method> annotatedMethods = Stream.of(testClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(ArtifactDependencies.class))
                .collect(Collectors.toList());

        if (annotatedMethods.size() > 1) {
            throw new IllegalArgumentException("Too many methods annotated with " + ArtifactDependencies.class.getName());
        }

        if (annotatedMethods.size() == 1) {
            final Method dependencyMethod = annotatedMethods.get(0);
            dependencyMethod.setAccessible(true);
            validate(dependencyMethod);

            this.container.setRequestedMavenArtifacts((List<String>) dependencyMethod.invoke(null));
        }

        // Gather test and provided dependencies
        final ShrinkwrapArtifactResolvingHelper resolvingHelper = ShrinkwrapArtifactResolvingHelper.defaultInstance();
        final MavenResolvedArtifact[] deps =
                resolvingHelper.withResolver(r -> MavenProfileLoader.loadPom(r)
                        .importDependencies(ScopeType.TEST, ScopeType.PROVIDED)
                        .resolve()
                        .withTransitivity()
                        .asResolvedArtifact());

        StringBuffer buffer = new StringBuffer();

        for (MavenResolvedArtifact artifact : deps) {
            buffer.append(artifact.asFile().getAbsolutePath());
            buffer.append(File.pathSeparator);
        }

        System.setProperty("swarm.test.dependencies", buffer.toString());
    }

    private void validate(Method dependencyMethod) {
        if (!Modifier.isStatic(dependencyMethod.getModifiers())) {
            throw new IllegalArgumentException("Method annotated with " + ArtifactDependencies.class.getName() + " is not static. " + dependencyMethod);
        }
        if (!List.class.isAssignableFrom(dependencyMethod.getReturnType())) {
            throw new IllegalArgumentException(
                    "Method annotated with " + ArtifactDependencies.class.getName() +
                            " must have return type " + List.class.getName() + ". " + dependencyMethod);
        }
        if (dependencyMethod.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Method annotated with " + ArtifactDependencies.class.getName() + " can not accept parameters. " + dependencyMethod);
        }
    }

    private WildFlySwarmContainer container;
}
