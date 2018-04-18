package io.thorntail.jpa.impl;

import java.util.HashMap;
import java.util.Map;

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
        Map props = new HashMap<>();
        props.put("hibernate.transaction.jta.platform",
                  "org.hibernate.engine.transaction.jta.platform.internal.JBossStandAloneJtaPlatform");
        EntityManager result = this.factory.createEntityManager(props);
        return result;
    }

    @Inject
    private EntityManagerFactory factory;
}
