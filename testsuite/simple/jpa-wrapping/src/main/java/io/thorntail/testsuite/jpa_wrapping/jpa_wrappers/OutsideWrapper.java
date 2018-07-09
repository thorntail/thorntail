package io.thorntail.testsuite.jpa_wrapping.jpa_wrappers;

import io.thorntail.jpa.EntityManagerWrapperFactory;
import javax.persistence.EntityManager;

/**
 * @author kg6zvp
 */
public class OutsideWrapper extends EntityManagerWrapperFactory {
    @Override
    public int order() {
        return 1;
    }

    @Override
    public EntityManager wrap(EntityManager em) {
        return new OutsideDelegate(em);
    }

    public static class OutsideDelegate extends EntityManagerDelegate {
        public OutsideDelegate(EntityManager em) {
            super(em);
        }
    }
}
