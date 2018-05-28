package io.thorntail.jpa.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;

/**
 * @author Ken Finnigan
 */
public class EntityManagerFactoryResourceProvider implements ResourceReferenceFactory<EntityManagerFactory>, ResourceReference<EntityManagerFactory> {

    private final String unitName;
    
    private EntityManagerFactory entityManagerFactory;

    private final Map<String, Object> properties;

    EntityManagerFactoryResourceProvider(String unitName) {
        this(unitName, Collections.emptyMap());
    }

    EntityManagerFactoryResourceProvider(String unitName, Map<String, String> properties) {
        this.unitName = unitName;
        this.properties = applyDefaultConfigurations(properties);
    }

    @Override
    public ResourceReference<EntityManagerFactory> createResource() {
        return this;
    }

    @Override
    public EntityManagerFactory getInstance() {
        if (null == entityManagerFactory) {
            entityManagerFactory = Persistence.createEntityManagerFactory(unitName, properties);
        }

        return entityManagerFactory;
    }

    @Override
    public void release() {
        //EntityManagerFactory must be a singleton for compliance with JSR-338
    }

    private static Map<String,Object> applyDefaultConfigurations(Map<String,String> original) {
        Map<String,Object> properties = new HashMap<>( original );
        applyDefault(properties, AvailableSettings.JTA_PLATFORM, NarayanaStandaloneJtaPlatform::new);
        return properties;
    }

    private static void applyDefault(Map<String,Object> properties, String key, Supplier<Object> valueSource) {
        properties.computeIfAbsent( key, (k) -> valueSource.get() );
    }

}
