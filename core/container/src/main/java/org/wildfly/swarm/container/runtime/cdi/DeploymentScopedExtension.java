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
package org.wildfly.swarm.container.runtime.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Created by bob on 5/12/17.
 */
public class DeploymentScopedExtension implements Extension {

    public DeploymentScopedExtension(DeploymentContext deploymentContext) {
        this.deploymentContext = deploymentContext;
    }

    public void registerContext(@Observes final AfterBeanDiscovery event, BeanManager beanManager) {

        // Register the deploymentContext
        event.addContext(this.deploymentContext);

        // Register the deploymentContext bean
        event.addBean(new Bean<DeploymentContext>() {

            @Override
            public DeploymentContext create(CreationalContext<DeploymentContext> creationalContext) {
                return new DeploymentContextImpl.InjectableDeploymentContext(deploymentContext, beanManager);
            }

            @Override
            public void destroy(DeploymentContext instance, CreationalContext<DeploymentContext> creationalContext) {
            }

            @Override
            public Set<Type> getTypes() {
                //return ImmutableSet.of(DeploymentContext.class);
                return Collections.unmodifiableSet(Collections.singleton(DeploymentContext.class));
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return Collections.unmodifiableSet(Collections.singleton(Default.Literal.INSTANCE));
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public Class<?> getBeanClass() {
                return DeploymentScopedExtension.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return Collections.emptySet();
            }

            @Override
            public boolean isNullable() {
                return false;
            }
        });

    }

    private final DeploymentContext deploymentContext;
}
