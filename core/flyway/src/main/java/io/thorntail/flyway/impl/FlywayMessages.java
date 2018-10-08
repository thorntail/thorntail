package io.thorntail.flyway.impl;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import io.thorntail.logging.impl.LoggingUtil;

@MessageLogger(projectCode = LoggingUtil.CODE, length = 6)
public interface FlywayMessages {

	FlywayMessages MESSAGES = Logger.getMessageLogger(FlywayMessages.class, LoggingUtil.loggerCategory("flyway"));

	// TODO: Move to kernel?
	int OFFSET = 90000;

	@LogMessage(level = Logger.Level.ERROR)
	@Message(id = OFFSET + 1, value = "datasource with name '%s' not found")
	void dataSourceNotFound(String jndiName);

	@LogMessage(level = Logger.Level.INFO)
	@Message(id = OFFSET + 2, value = FlywayConfiguration.DATASOURCE_PROPERTY
			+ "not set, no migration with flyway performed")
	void dataSourceIdNotConfigured();

	@LogMessage(level = Logger.Level.DEBUG)
	@Message(id = OFFSET + 3, value = "fetching datasource metadata for '%s'")
	void fetchingDatasourceMetadataFor(String dataSourceId);

	@LogMessage(level = Logger.Level.DEBUG)
	@Message(id = OFFSET + 4, value = "fetching datasource with JNDI name '%s'")
	void fetchingDatasourceWithJndiName(String jndiName);

	@LogMessage(level = Logger.Level.INFO)
	@Message(id = OFFSET + 5, value = "performing flyway migration")
	void performingFlywayMigration();

	@LogMessage(level = Logger.Level.INFO)
	@Message(id = OFFSET + 6, value = "flyway migration failed : %s")
	void flywayMigrationFailed(String message);
}
