package io.thorntail.testsuite.jpa_wrapping.jpa_wrappers;

import io.thorntail.jpa.EntityManagerWrapperFactory;
import javax.persistence.EntityManager;

/**
 * @author kg6zvp
 */
public class InsideWrapper extends EntityManagerWrapperFactory {
    @Override
    public int order() {
        return 0;
    }

    @Override
    public EntityManager wrap(EntityManager em) {
        return new InsideDelegate(em);
    }

    public static class InsideDelegate extends EntityManagerDelegate {
        public InsideDelegate(EntityManager em) {
            super(em);
        }
    }
}
