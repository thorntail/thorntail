package org.jboss.unimbus.jpa;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;

/**
 * @author Ken Finnigan
 */
public class EntityManagerResourceProvider implements ResourceReferenceFactory<EntityManager> {

    private final String unitName;

    private final Map<String, String> properties;

    private EntityManagerFactory entityManagerFactory;

    EntityManagerResourceProvider(String unitName) {
        this(unitName, Collections.emptyMap());
    }

    EntityManagerResourceProvider(String unitName, Map<String, String> properties) {
        this.unitName = unitName;
        this.properties = properties;
    }

    @Override
    public ResourceReference<EntityManager> createResource() {
        return new ResourceReference<EntityManager>() {
            private EntityManager entityManager;

            @Override
            public EntityManager getInstance() {
                if (null == entityManager && null == entityManagerFactory) {
                    entityManagerFactory =
                            new EntityManagerFactoryResourceProvider(unitName)
                                    .createResource()
                                    .getInstance();
                }

                if (null == entityManager) {
                    entityManager = entityManagerFactory.createEntityManager(properties);
                }

                return entityManager;
            }

            @Override
            public void release() {
                if (null != entityManager) {
                    entityManager.close();
                }
            }
        };
    }
}
