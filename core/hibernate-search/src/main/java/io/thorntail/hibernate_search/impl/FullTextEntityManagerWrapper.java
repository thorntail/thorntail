package io.thorntail.hibernate_search.impl;

import io.thorntail.jpa.EntityManagerWrapperFactory;
import javax.persistence.EntityManager;
import org.hibernate.search.jpa.Search;

/**
 * @author kg6zvp
 */
public class FullTextEntityManagerWrapper extends EntityManagerWrapperFactory {
    @Override
    public int order() {
        return Integer.MAX_VALUE; // should come last
    }

    @Override
    public EntityManager wrap(EntityManager em) {
        return Search.getFullTextEntityManager(em);
    }
}
