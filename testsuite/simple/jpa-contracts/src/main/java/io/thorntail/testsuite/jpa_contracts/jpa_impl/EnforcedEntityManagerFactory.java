package io.thorntail.testsuite.jpa_contracts.jpa_impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @author smccollum
 */
public final class EnforcedEntityManagerFactory implements EntityManagerFactory {
	public static Map<String, AtomicInteger> inst_count = new ConcurrentHashMap<>();
	public static Map<String, Set<EnforcedEntityManagerFactory>> inst = new ConcurrentHashMap<>();

	Map<String, Object> properties;

	AtomicBoolean open;

	public EnforcedEntityManagerFactory(String unitName, Map<String, Object> properties){
		if(!inst_count.containsKey(unitName)) inst_count.put(unitName, new AtomicInteger(0));
		if(!inst.containsKey(unitName)) inst.put(unitName, ConcurrentHashMap.newKeySet());
		
		inst_count.get(unitName).incrementAndGet();
		

		open = new AtomicBoolean(true);
		this.properties = properties;
		inst.get(unitName).add(this);
	}

	@Override
	public EntityManager createEntityManager() {
		return null;
	}

	@Override
	public EntityManager createEntityManager(Map map) {
		return null;
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType) {
		return null;
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		return null;
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}

	@Override
	public void close() {
		open.set(false);
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public Cache getCache() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addNamedQuery(String name, Query query) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
