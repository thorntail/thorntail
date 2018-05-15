package io.thorntail.jpa.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import io.thorntail.jpa.impl.opentracing.TracedEntityManagerResourceProvider;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import io.thorntail.TraceMode;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;

/**
 * @author Ken Finnigan
 */
public class JpaServices implements JpaInjectionServices {

    private String emptyScopeDefaultName;

    private static Map<String, ResourceReferenceFactory<EntityManagerFactory>> emfs = new ConcurrentHashMap<>();

    private static Map<String, ResourceReferenceFactory<EntityManager>> ems = new ConcurrentHashMap<>();
    
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
        if (traceMode.isPresent() && traceMode.get() != TraceMode.OFF) {
            JpaMessages.MESSAGES.tracingEnabled(scopedPuName);

            if (!ems.containsKey(scopedPuName)) {
                ems.put(scopedPuName, new TracedEntityManagerResourceProvider(traceMode.get(), scopedPuName));
            }
        }

        if (!ems.containsKey(scopedPuName)) {
            ems.put(scopedPuName, new EntityManagerResourceProvider(scopedPuName));
        }

        return ems.get(scopedPuName);
    }

    @Override
    public ResourceReferenceFactory<EntityManagerFactory> registerPersistenceUnitInjectionPoint(InjectionPoint injectionPoint) {
        final PersistenceUnit context = getResourceAnnotated(injectionPoint).getAnnotation(PersistenceUnit.class);
        if (context == null) {
            throw JpaMessages.MESSAGES.annotationNotFound(PersistenceUnit.class, injectionPoint.getMember());
        }

        String scopedPuName = getScopedPuName(context.unitName());
        return getEntityManagerFactory(scopedPuName);
    }

    public static ResourceReferenceFactory<EntityManagerFactory> getEntityManagerFactory(String scopedPuName) {
        JpaMessages.MESSAGES.createFactoryForPersistence(PersistenceUnit.class, scopedPuName);

        if (!emfs.containsKey(scopedPuName)) {
            emfs.put(scopedPuName, new EntityManagerFactoryResourceProvider(scopedPuName));
        }

        return emfs.get(scopedPuName);
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
        for(ResourceReferenceFactory<EntityManagerFactory> rrf : emfs.values()){
            rrf.createResource().getInstance().close();
        }
        emfs.clear();
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
                :
                (null != this.emptyScopeDefaultName
                        ? this.emptyScopeDefaultName : getPersistentUnitNameFromDescriptor());
    }

    private String getPersistentUnitNameFromDescriptor() {
        this.emptyScopeDefaultName = new PersistenceUnitDescriptorProducer().persistenceUnitDescriptor().getName();
        return this.emptyScopeDefaultName;
    }
}
