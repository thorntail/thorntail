package io.thorntail.testsuite.jpa_contracts.jpa_impl;

import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

/**
 *
 * @author smccollum
 */
public class EnforcerPersistenceProvider implements PersistenceProvider {
	@Override
	public EntityManagerFactory createEntityManagerFactory(String puName, Map properties) {
		return new EnforcedEntityManagerFactory(puName, properties);
	}

	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
		throw new UnsupportedOperationException("createContainerEntityManagerFactory not supported.");
	}

	@Override
	public void generateSchema(PersistenceUnitInfo info, Map map) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean generateSchema(String persistenceUnitName, Map map) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ProviderUtil getProviderUtil() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
