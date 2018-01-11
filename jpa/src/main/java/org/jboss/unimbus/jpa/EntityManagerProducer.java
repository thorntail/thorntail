package org.jboss.unimbus.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@ApplicationScoped
public class EntityManagerProducer {

    @Produces
    @ApplicationScoped
    EntityManager entityManager() {
        return entityManagerFactory.createEntityManager();
    }

    @Inject
    private EntityManagerFactory entityManagerFactory;
}
