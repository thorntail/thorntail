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
package org.wildfly.swarm.arquillian.adapter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmObserver {

    private WildFlySwarmContainer container;

    public void afterSetup(@Observes final AfterSetup event) throws Exception {
        this.container = (WildFlySwarmContainer) event.getDeployableContainer();
    }

    public void generate(@Observes(precedence = 100) final GenerateDeployment event) throws Exception {
        this.container.setTestClass(event.getTestClass().getJavaClass());

        List<Method> annotatedMethods = ReflectionHelper.getMethodsWithAnnotation(event.getTestClass().getJavaClass(), ArtifactDependencies.class);

        if (annotatedMethods.size() > 1) {
            throw new IllegalArgumentException("Too many methods annotated with " + ArtifactDependencies.class.getName());
        }

        if (annotatedMethods.size() == 1) {
            Method dependencyMethod = annotatedMethods.get(0);
            validate(dependencyMethod);

            this.container.setRequestedMavenArtifacts((List<String>) dependencyMethod.invoke(null));
        }
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
}
