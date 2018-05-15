package io.thorntail.jpa.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

@ApplicationScoped
public class EntityManagerFactoryProducer {

    @Inject
    private PersistenceUnitDescriptor persistenceUnitDescriptor;

    @Produces
    @ApplicationScoped
    EntityManagerFactory entityManager() {
        return JpaServices.getEntityManagerFactory(persistenceUnitDescriptor.getName()).createResource().getInstance();
    }
}
