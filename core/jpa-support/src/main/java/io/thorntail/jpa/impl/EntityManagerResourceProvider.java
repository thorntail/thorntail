package io.thorntail.jpa.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    protected final boolean useFullTextEntityManager;

    protected Class<?> hibernateSearchClazz = null;

    protected EntityManagerResourceProvider(String unitName) {
        this(unitName, Collections.emptyMap());
    }

    protected EntityManagerResourceProvider(String unitName, Map<String, String> properties) {
        this.unitName = unitName;
        this.properties = properties;
        this.useFullTextEntityManager = isHibernateSearchPresent();
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
                    if (useFullTextEntityManager) {
                        entityManager = wrapWithSearch(entityManager);
                    }
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
        return em;
    }

    protected EntityManager wrapWithSearch(EntityManager em) {
        try{
            Method fullTextWrapper = this.hibernateSearchClazz.getMethod("getFullTextEntityManager", EntityManager.class);
            return (EntityManager)fullTextWrapper.invoke(null, em);
        } catch (NoSuchMethodException|SecurityException|IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
            throw JpaMessages.MESSAGES.errorWrappingEntityManagerForSearch(ex);
        }
    }

    private boolean isHibernateSearchPresent() {
        try{
            this.hibernateSearchClazz = Class.forName("org.hibernate.search.jpa.Search");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
