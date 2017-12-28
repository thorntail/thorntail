/**
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;

public class ClaimProviderBeanAttributes implements BeanAttributes<Object> {
    /**
     * Decorate the ConfigPropertyProducer BeanAttributes to set the types the producer applies to. This set is collected
     * from all injection points annotated with @ConfigProperty.
     *
     * @param delegate - the original producer method BeanAttributes
     * @param types    - the full set of @ConfigProperty injection point types
     */
    public ClaimProviderBeanAttributes(BeanAttributes<Object> delegate, Set<Type> types, Set<Annotation> qualifiers) {
        this.delegate = delegate;
        this.types = types;
        this.qualifiers = qualifiers;
        if (types.size() == 0) {
            Thread.dumpStack();
        }
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return delegate.getScope();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return delegate.getStereotypes();
    }

    @Override
    public boolean isAlternative() {
        return delegate.isAlternative();
    }

    private BeanAttributes<Object> delegate;

    private Set<Type> types;

    private Set<Annotation> qualifiers;

}
