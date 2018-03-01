package org.jboss.unimbus.jpa.impl;

import java.util.Optional;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.unimbus.TraceMode;
import org.jboss.unimbus.jpa.impl.opentracing.TracedEntityManagerResourceProvider;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;

/**
 * @author Ken Finnigan
 */
public class JpaServices implements JpaInjectionServices {

    @Override
    public ResourceReferenceFactory<EntityManager> registerPersistenceContextInjectionPoint(InjectionPoint injectionPoint) {
        final PersistenceContext context = getResourceAnnotated(injectionPoint).getAnnotation(PersistenceContext.class);
        if (context == null) {
            throw JpaMessages.MESSAGES.annotationNotFound(PersistenceContext.class, injectionPoint.getMember());
        }

        String scopedPuName = getScopedPuName(context.unitName());
        JpaMessages.MESSAGES.createFactoryForPersistence(PersistenceContext.class, scopedPuName);

        Config config = ConfigProviderResolver.instance().getConfig();

        Optional<TraceMode> traceMode = config.getOptionalValue("jpa." + scopedPuName + ".trace", TraceMode.class);
        if ( traceMode.isPresent() &&  traceMode.get() != TraceMode.OFF ) {
            JpaMessages.MESSAGES.tracingEnabled(scopedPuName);
            return new TracedEntityManagerResourceProvider(traceMode.get(), scopedPuName);
        }

        return new EntityManagerResourceProvider(scopedPuName);
    }

    @Override
    public ResourceReferenceFactory<EntityManagerFactory> registerPersistenceUnitInjectionPoint(InjectionPoint injectionPoint) {
        final PersistenceUnit context = getResourceAnnotated(injectionPoint).getAnnotation(PersistenceUnit.class);
        if (context == null) {
            throw JpaMessages.MESSAGES.annotationNotFound(PersistenceUnit.class, injectionPoint.getMember());
        }

        String scopedPuName = getScopedPuName(context.unitName());
        JpaMessages.MESSAGES.createFactoryForPersistence(PersistenceUnit.class, scopedPuName);

        return new EntityManagerFactoryResourceProvider(scopedPuName);
    }

    @Override
    public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint) {
        return registerPersistenceContextInjectionPoint(injectionPoint).createResource().getInstance();
    }

    @Override
    public EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint) {
        return registerPersistenceUnitInjectionPoint(injectionPoint).createResource().getInstance();
    }

    @Override
    public void cleanup() {

    }

    private static Annotated getResourceAnnotated(InjectionPoint injectionPoint) {
        if (injectionPoint instanceof ParameterInjectionPoint) {
            return ((ParameterInjectionPoint<?, ?>) injectionPoint).getAnnotated().getDeclaringCallable();
        }
        return injectionPoint.getAnnotated();
    }

    private String getScopedPuName(String unitName) {
        return (null != unitName && unitName.trim().length() > 0)
                ? unitName
                : new PersistenceUnitDescriptorProducer().persistenceUnitDescriptor().getName();
    }
}
