package io.thorntail.hibernate_search.impl;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;
import io.thorntail.hibernate_search.QueryBuilderContext;
import static io.thorntail.hibernate_search.impl.InjectionPointUtils.getAnnotation;
import javax.enterprise.inject.Disposes;

/**
 * @author kg6zvp
 */
public class QueryFactory {
    @Produces
    @QueryBuilderContext(Object.class)
    public QueryBuilder getQueryBuilder(InjectionPoint ip) {
        QueryBuilderContext qbc = getAnnotation(ip, QueryBuilderContext.class);
        FullTextEntityManager em = EntityManagerUtils.getFullTextEntityManager(qbc.persistenceUnit());
        QueryBuilder qb = em.getSearchFactory().buildQueryBuilder().forEntity(qbc.value()).get();
        return new QueryBuilderDelegate(qb, em);
    }

    public void disposeQueryBuilder(@Disposes @QueryBuilderContext(Object.class) QueryBuilder qb) {
        ((QueryBuilderDelegate)qb).close();
    }
}
