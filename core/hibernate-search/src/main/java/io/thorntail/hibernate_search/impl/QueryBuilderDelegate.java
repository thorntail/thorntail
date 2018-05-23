package io.thorntail.hibernate_search.impl;

import javax.persistence.EntityManager;
import org.hibernate.search.query.dsl.AllContext;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.FacetContext;
import org.hibernate.search.query.dsl.MoreLikeThisContext;
import org.hibernate.search.query.dsl.PhraseContext;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.RangeContext;
import org.hibernate.search.query.dsl.SimpleQueryStringContext;
import org.hibernate.search.query.dsl.SpatialContext;
import org.hibernate.search.query.dsl.TermContext;
import org.hibernate.search.query.dsl.sort.SortContext;

/**
 * @author kg6zvp
 */
public class QueryBuilderDelegate implements QueryBuilder {
    QueryBuilder delegate;

    EntityManager em;

    public QueryBuilderDelegate(QueryBuilder delegate, EntityManager em) {
        this.delegate = delegate;
        this.em = em;
    }

    public void close() {
        em.close();
    }

    @Override
    public TermContext keyword() {
        return delegate.keyword();
    }

    @Override
    public RangeContext range() {
        return delegate.range();
    }

    @Override
    public PhraseContext phrase() {
        return delegate.phrase();
    }

    @Override
    public SimpleQueryStringContext simpleQueryString() {
        return delegate.simpleQueryString();
    }

    @Override
    public BooleanJunction<BooleanJunction> bool() {
        return delegate.bool();
    }

    @Override
    public AllContext all() {
        return delegate.all();
    }

    @Override
    public FacetContext facet() {
        return delegate.facet();
    }

    @Override
    public SpatialContext spatial() {
        return delegate.spatial();
    }

    @Override
    public MoreLikeThisContext moreLikeThis() {
        return delegate.moreLikeThis();
    }

    @Override
    public SortContext sort() {
        return delegate.sort();
    }

    public QueryBuilder getDelegate() {
        return this.delegate;
    }
}
