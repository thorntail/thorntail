package io.thorntail.testsuite.jpa_wrapping.jpa_wrappers;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

/**
 * @author kg6zvp
 */
public class EntityManagerDelegate implements EntityManager {
    EntityManager entityManagerDelegate;

    public EntityManagerDelegate(EntityManager em){
        this.entityManagerDelegate = em;
    }

    public EntityManager getEntityManagerDelegate() {
        return this.entityManagerDelegate;
    }

    @Override
    public void persist(Object entity) {
        entityManagerDelegate.persist(entity);
    }

    @Override
    public <T> T merge(T entity) {
        return entityManagerDelegate.merge(entity);
    }

    @Override
    public void remove(Object entity) {
        entityManagerDelegate.remove(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return entityManagerDelegate.find(entityClass, primaryKey);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return entityManagerDelegate.find(entityClass, primaryKey, properties);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return entityManagerDelegate.find(entityClass, primaryKey, lockMode);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return entityManagerDelegate.find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return entityManagerDelegate.getReference(entityClass, primaryKey);
    }

    @Override
    public void flush() {
        entityManagerDelegate.flush();
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        entityManagerDelegate.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return entityManagerDelegate.getFlushMode();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        entityManagerDelegate.lock(entity, lockMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        entityManagerDelegate.lock(entity, lockMode, properties);
    }

    @Override
    public void refresh(Object entity) {
        entityManagerDelegate.refresh(entity);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        entityManagerDelegate.refresh(entity, properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        entityManagerDelegate.refresh(entity, lockMode);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        entityManagerDelegate.refresh(entity, lockMode, properties);
    }

    @Override
    public void clear() {
        entityManagerDelegate.clear();
    }

    @Override
    public void detach(Object entity) {
        entityManagerDelegate.detach(entity);
    }

    @Override
    public boolean contains(Object entity) {
        return entityManagerDelegate.contains(entity);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return entityManagerDelegate.getLockMode(entity);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        entityManagerDelegate.setProperty(propertyName, value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return entityManagerDelegate.getProperties();
    }

    @Override
    public Query createQuery(String qlString) {
        return entityManagerDelegate.createQuery(qlString);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return entityManagerDelegate.createQuery(criteriaQuery);
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        return entityManagerDelegate.createQuery(updateQuery);
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        return entityManagerDelegate.createQuery(deleteQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return entityManagerDelegate.createQuery(qlString, resultClass);
    }

    @Override
    public Query createNamedQuery(String name) {
        return entityManagerDelegate.createNamedQuery(name);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return entityManagerDelegate.createNamedQuery(name, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        return entityManagerDelegate.createNativeQuery(sqlString);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return entityManagerDelegate.createNativeQuery(sqlString, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return entityManagerDelegate.createNativeQuery(sqlString, resultSetMapping);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return entityManagerDelegate.createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return entityManagerDelegate.createNamedStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return entityManagerDelegate.createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return entityManagerDelegate.createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public void joinTransaction() {
        entityManagerDelegate.joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return entityManagerDelegate.isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return entityManagerDelegate.unwrap(cls);
    }

    @Override
    public Object getDelegate() {
        return entityManagerDelegate.getDelegate();
    }

    @Override
    public void close() {
        entityManagerDelegate.close();
    }

    @Override
    public boolean isOpen() {
        return entityManagerDelegate.isOpen();
    }

    @Override
    public EntityTransaction getTransaction() {
        return entityManagerDelegate.getTransaction();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerDelegate.getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManagerDelegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return entityManagerDelegate.getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return entityManagerDelegate.createEntityGraph(rootType);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return entityManagerDelegate.createEntityGraph(graphName);
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return entityManagerDelegate.getEntityGraph(graphName);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return entityManagerDelegate.getEntityGraphs(entityClass);
    }
}
