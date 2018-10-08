package io.thorntail.flyway.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.NoSuchElementException;
import java.util.Spliterators;

import org.eclipse.microprofile.config.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.thorntail.datasources.impl.DataSourceRegistry;

@RunWith(MockitoJUnitRunner.class)
public class FlywayConfigurationTest {

	@Mock
	private Config config;

	@Mock
	private DataSourceRegistry dataSourceRegistry;

	@Mock
	private FlywayMessages flywayMessages;

	@InjectMocks
	private FlywayConfiguration flywayConfiguration;

	@Test
	public void shouldNotDoAnythingIfDataSourcePropertyNotSet() {

		given(config.getValue(FlywayConfiguration.DATASOURCE_PROPERTY, String.class))
				.willThrow(NoSuchElementException.class);

		flywayConfiguration.init();

		verify(flywayMessages).dataSourceIdNotConfigured();
		verifyZeroInteractions(dataSourceRegistry);
	}

	@Test
	public void dataSourceShouldBeNullWhenNoDatabaseMetaDataFound() {

		given(config.getValue(FlywayConfiguration.DATASOURCE_PROPERTY, String.class)).willReturn("datasource");

		given(dataSourceRegistry.spliterator()).willReturn(Spliterators.emptySpliterator());

		flywayConfiguration.init();

		verify(config, never()).getPropertyNames();

		assertThat(flywayConfiguration.configured()).isEqualTo(false);
	}
}