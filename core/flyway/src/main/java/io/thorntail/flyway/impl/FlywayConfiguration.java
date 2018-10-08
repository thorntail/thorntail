package io.thorntail.flyway.impl;

import java.util.Optional;
import java.util.Properties;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.microprofile.config.Config;

import io.thorntail.datasources.DataSourceMetaData;
import io.thorntail.datasources.impl.DataSourceRegistry;

@ApplicationScoped
public class FlywayConfiguration {

	public static final String PREFIX = "flyway.";

	public static final String DATASOURCE_PROPERTY = PREFIX + "datasource.id";

	@Inject
	private Config config;

	@Inject
	private DataSourceRegistry dataSourceRegistry;

	@Inject
	private FlywayMessages flywayMessages;

	private Properties properties = new Properties();

	private DataSource dataSource = null;

	@PostConstruct
	public void init() {
		String dataSourceId;
		try {
			dataSourceId = config.getValue(DATASOURCE_PROPERTY, String.class);
		} catch (Exception e) {
			flywayMessages.dataSourceIdNotConfigured();
			return;
		}

		flywayMessages.fetchingDatasourceMetadataFor(dataSourceId);
		Optional<DataSourceMetaData> dataSourceMetaData = StreamSupport.stream(dataSourceRegistry.spliterator(), false)
				.filter(ds -> ds.getId().equals(dataSourceId)).findFirst();

		dataSource = (DataSource) dataSourceMetaData.map(ds -> {
			try {
				flywayMessages.fetchingDatasourceWithJndiName(ds.getJNDIName());
				// TODO: Not really loving this. Requires datasources and JNDI, solve in another
				// way?
				return new InitialContext().lookup(ds.getJNDIName());
			} catch (NamingException e) {
				flywayMessages.dataSourceNotFound(ds.getJNDIName());
				// TODO: Throw exception? Property configured, but could not find in JNDI
				return null;
			}
		}).orElse(null);

		if (configured()) {
			StreamSupport.stream(config.getPropertyNames().spliterator(), false)
					.filter(p -> !p.equals(DATASOURCE_PROPERTY)).filter(p -> p.startsWith(PREFIX))
					.forEach(p -> properties.put(p, config.getValue(p, String.class)));
		}
	}

	public Properties properties() {
		return properties;
	}

	public DataSource dataSource() {
		return dataSource;
	}

	public boolean configured() {
		return dataSource != null;
	}
}