package io.thorntail.hibernate_search.impl;

import io.thorntail.hibernate_search.EntityManagerContext;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import org.hibernate.search.jpa.FullTextEntityManager;

/**
 * @author kg6zvp
 */
public class EntityManagerFactory {
    @Produces
    @EntityManagerContext
    public FullTextEntityManager getFullTextEntityManager(InjectionPoint ip) {
        EntityManagerContext ctx = InjectionPointUtils.getAnnotation(ip, EntityManagerContext.class);
        return EntityManagerUtils.getFullTextEntityManager(ctx.value());
    }

    @Produces
    public FullTextEntityManager getFullTextEntityManager() {
        return EntityManagerUtils.getFullTextEntityManager("");
    }

    public void discardFullTextEntityManager(@Disposes FullTextEntityManager em) {
        em.close();
    }

    public void discardQualifiedFullTextEntityManager(@Disposes @EntityManagerContext FullTextEntityManager em) {
        em.close();
    }
}
