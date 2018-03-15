package org.jboss.unimbus.jpa.impl;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.jboss.unimbus.events.LifecycleEvent;

/**
 *
 * This bean force Persistence unit initialization at startup to avoid db initialization to be included
 * in first query transaction
 *
 *
 * @author Antoine Sabot-Durand
 *
 */

@ApplicationScoped
public class EntityManagerInitializer {

    @PersistenceUnit
    EntityManagerFactory emf;


    void produceOnStartup(@Observes LifecycleEvent.AfterStart lifecycleEvent) {
        JpaMessages.MESSAGES.persistencecontextInitialization();
    }
}
