package io.thorntail.testsuite.hibernate_search;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 *
 * @author smccollum
 */
@RequestScoped
public class UserDao {
    @Inject
    QueryBuilder qb;
}
