package io.thorntail.jwt.auth.impl;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;

/**
 * An implementation of BeanAttributes<Object> that wraps the generic producer BeanAttributes
 * to allow the MPJWTExtension to collect the types of all corresponding injection sites
 */
public class ClaimProviderBeanAttributes implements BeanAttributes<Object> {
    /**
     * Decorate the ConfigPropertyProducer BeanAttributes to set the types the producer applies to. This set is collected
     * from all injection points annotated with @ConfigProperty.
     *
     * @param delegate - the original producer method BeanAttributes
     * @param types    - the full set of @Claim injection point types
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
