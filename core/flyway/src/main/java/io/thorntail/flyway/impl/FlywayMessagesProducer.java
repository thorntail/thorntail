package io.thorntail.flyway.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.logging.Logger;

import io.thorntail.logging.impl.LoggingUtil;

@ApplicationScoped
public class FlywayMessagesProducer {

	@Produces
	@ApplicationScoped
	public FlywayMessages flywayMessages() {
		return Logger.getMessageLogger(FlywayMessages.class, LoggingUtil.loggerCategory("flyway"));
	}
}