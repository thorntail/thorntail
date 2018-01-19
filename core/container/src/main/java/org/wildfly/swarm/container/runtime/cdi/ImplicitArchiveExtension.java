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
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.container.runtime.ImplicitDeployment;

/**
 * @author Ken Finnigan
 */
public class ImplicitArchiveExtension implements Extension {

    <T> void processBeanAttributes(@Observes ProcessBeanAttributes<T> pba, BeanManager beanManager) throws Exception {
        final BeanAttributes<T> beanAttributes = pba.getBeanAttributes();
        if (beanAttributes.getTypes().contains(Archive.class)) {
            if (!DeploymentScoped.class.isAssignableFrom(beanAttributes.getScope())) {
                pba.setBeanAttributes(new BeanAttributes<T>() {
                    @Override
                    public Set<Type> getTypes() {
                        return beanAttributes.getTypes();
                    }

                    @Override
                    public Set<Annotation> getQualifiers() {
                        Set<Annotation> qualifiers = new HashSet<>();
                        qualifiers.addAll(beanAttributes.getQualifiers());
                        qualifiers.add(ImplicitDeployment.Literal.INSTANCE);
                        qualifiers.removeIf(e -> Default.class.isAssignableFrom(e.getClass()));
                        return qualifiers;
                    }

                    @Override
                    public Class<? extends Annotation> getScope() {
                        return beanAttributes.getScope();
                    }

                    @Override
                    public String getName() {
                        return beanAttributes.getName();
                    }

                    @Override
                    public Set<Class<? extends Annotation>> getStereotypes() {
                        return beanAttributes.getStereotypes();
                    }

                    @Override
                    public boolean isAlternative() {
                        return beanAttributes.isAlternative();
                    }
                });
            }
        }
    }


}
