package io.thorntail.flyway.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.flywaydb.core.Flyway;

import io.thorntail.events.LifecycleEvent;

@ApplicationScoped
public class FlywayMigrator {

	@Inject
	private FlywayConfiguration flywayConfiguration;

	public void init(@Observes LifecycleEvent.Deploy event) {
		if (flywayConfiguration.configured()) {
			FlywayMessages.MESSAGES.performingFlywayMigration();
			try {
				Flyway flyway = Flyway.configure().dataSource(flywayConfiguration.dataSource())
						.configuration(flywayConfiguration.properties()).load();
				flyway.migrate();
			} catch (Exception e) {
				FlywayMessages.MESSAGES.flywayMigrationFailed(e.getMessage());
			}
		}
	}
}
