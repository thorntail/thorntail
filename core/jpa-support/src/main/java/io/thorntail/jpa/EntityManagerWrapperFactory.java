package io.thorntail.jpa;

import javax.persistence.EntityManager;

/**
 * @author kg6zvp
 */
public abstract class EntityManagerWrapperFactory implements Comparable<EntityManagerWrapperFactory> {
    @Override
    public int compareTo(EntityManagerWrapperFactory other) {
        return this.order() - other.order();
    }

    public abstract int order();

    public abstract EntityManager wrap(EntityManager em);
}
