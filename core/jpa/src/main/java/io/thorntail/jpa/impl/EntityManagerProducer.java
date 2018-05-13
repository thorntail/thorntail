package io.thorntail.jpa.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@ApplicationScoped
public class EntityManagerProducer {

    @Inject
    private EntityManagerFactory factory;

    @Produces
    @ApplicationScoped
    EntityManager entityManager() {
        return factory.createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager em){
        em.close();
    }
}
