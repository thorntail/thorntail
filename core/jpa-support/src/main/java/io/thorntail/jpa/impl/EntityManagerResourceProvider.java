package io.thorntail.jpa.impl;

import io.thorntail.jpa.EntityManagerWrapperFactory;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;

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

    protected LinkedList<EntityManagerWrapperFactory> wrappers;

    protected EntityManagerResourceProvider(String unitName) {
        this(unitName, Collections.emptyMap());
    }

    protected EntityManagerResourceProvider(String unitName, Map<String, String> properties) {
        this.unitName = unitName;
        this.properties = properties;
        this.wrappers = loadWrappers();
        Collections.sort(wrappers);
    }

    protected LinkedList<EntityManagerWrapperFactory> loadWrappers() {
        LinkedList<EntityManagerWrapperFactory> wrappers = new LinkedList<>();
        ServiceLoader.load(EntityManagerWrapperFactory.class)
                         .forEach(wrappers::add);
        return wrappers;
    }

    @Override
    public ResourceReference<EntityManager> createResource() {
        return new ResourceReference<EntityManager>() {
            private EntityManager entityManager;

            @Override
            public EntityManager getInstance() {
                if (null == entityManager && null == entityManagerFactory) {
                        entityManagerFactory =
                            JpaServices.getEntityManagerFactory(unitName)
                                .createResource()
                                .getInstance();
                }

                if (null == entityManager) {
                    entityManager = wrap( entityManagerFactory.createEntityManager(properties) );
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

    protected EntityManager wrap(EntityManager em) {
        EntityManager currentEm = em;
        for(EntityManagerWrapperFactory emwf : wrappers) {
            currentEm = emwf.wrap(currentEm);
        }
        return currentEm;
    }
}
