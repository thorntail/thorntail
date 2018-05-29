package io.thorntail.testsuite.hibernate_search;

import io.thorntail.hibernate_search.EntityManagerContext;
import io.thorntail.hibernate_search.QueryBuilderContext;
import io.thorntail.hibernate_search.impl.QueryBuilderDelegate;
import io.thorntail.test.ThorntailTestRunner;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * @author kg6zvp
 */
@RunWith(ThorntailTestRunner.class)
public class HibernateSearchAppTest {
    @PersistenceContext
    EntityManager em;

    @Inject
    FullTextEntityManager searchEm;

    @Inject
    @EntityManagerContext("SearchPU")
    FullTextEntityManager searchEmScoped;

    @Inject
    @QueryBuilderContext(AppUser.class)
    QueryBuilder userQueryBuilder;

    @Inject
    @QueryBuilderContext(value = AppUser.class, persistenceUnit = "SearchPU")
    QueryBuilder userQueryBuilderScoped;

    @Test
    public void test() {
        assertThat(em).isNotNull();
        assertThat(em).isInstanceOf(FullTextEntityManager.class);

        assertThat(searchEm).isNotNull();
        assertThat(searchEmScoped).isNotNull();

        assertThat(userQueryBuilder).isNotNull();
        assertThat(userQueryBuilder).isInstanceOf(QueryBuilderDelegate.class);

        assertThat(userQueryBuilderScoped).isNotNull();
        assertThat(userQueryBuilderScoped).isInstanceOf(QueryBuilderDelegate.class);
    }
}
