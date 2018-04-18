package io.thorntail.jpa.impl;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;

/**
 * @author Ken Finnigan
 */
public class EntityManagerFactoryResourceProvider implements ResourceReferenceFactory<EntityManagerFactory> {

    private final String unitName;

    private final Map<String, String> properties;

    EntityManagerFactoryResourceProvider(String unitName) {
        this(unitName, Collections.emptyMap());
    }

    EntityManagerFactoryResourceProvider(String unitName, Map<String, String> properties) {
        this.unitName = unitName;
        this.properties = properties;
    }

    @Override
    public ResourceReference<EntityManagerFactory> createResource() {
        return new ResourceReference<EntityManagerFactory>() {
            private EntityManagerFactory entityManagerFactory;

            @Override
            public EntityManagerFactory getInstance() {
                if (null == entityManagerFactory) {
                    entityManagerFactory = Persistence.createEntityManagerFactory(unitName, properties);
                }

                return entityManagerFactory;
            }

            @Override
            public void release() {
                if (null != entityManagerFactory) {
                    entityManagerFactory.close();
                }
            }
        };
    }
}
