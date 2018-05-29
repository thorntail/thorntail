package io.thorntail.hibernate_search.impl;

import io.thorntail.jpa.impl.JpaServices;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

/**
 * @author kg6zvp
 */
public class EntityManagerUtils {
    static FullTextEntityManager getFullTextEntityManager(String puName) {
        return Search.getFullTextEntityManager(JpaServices.getEntityManagerFactory(JpaServices.getScopedPuName(puName)).createResource().getInstance().createEntityManager());
    }
}
