package io.thorntail.jpa.impl;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

@ApplicationScoped
public class EntityManagerFactoryProducer {

    @Produces
    @ApplicationScoped
    EntityManagerFactory entityManager() {

        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilderImpl(
                this.persistenceUnitDescriptor,
                Collections.emptyMap()
        );

        return builder.build();
    }

    @Inject
    private PersistenceUnitDescriptor persistenceUnitDescriptor;

}
