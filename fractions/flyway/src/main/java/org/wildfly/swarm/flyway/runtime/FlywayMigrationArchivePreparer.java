/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.flyway.runtime;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.flyway.FlywayFraction;
import org.wildfly.swarm.flyway.deployment.FlywayMigrationServletContextListener;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@DeploymentScoped
public class FlywayMigrationArchivePreparer implements DeploymentProcessor {

    private final Archive<?> archive;

    @Inject
    private Instance<DatasourcesFraction> dsFractionInstance;

    @Inject
    private Instance<FlywayFraction> flywayFractionInstance;

    @Inject
    public FlywayMigrationArchivePreparer(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() {
        if (archive.getName().endsWith(".war")) {
            WARArchive webArchive = archive.as(WARArchive.class);
            WebXmlAsset webXml = webArchive.findWebXmlAsset();
            webXml.addListener("org.wildfly.swarm.flyway.deployment.FlywayMigrationServletContextListener");
            FlywayFraction flywayFraction = flywayFractionInstance.get();
            if (flywayFraction.usePrimaryDataSource()) {
                String dataSourceJndi = getDatasourceNameJndi();
                webXml.setContextParam(FlywayMigrationServletContextListener.FLYWAY_JNDI_DATASOURCE, dataSourceJndi);
            } else {
                webXml.setContextParam(FlywayMigrationServletContextListener.FLYWAY_JDBC_URL, flywayFraction.jdbcUrl());
                webXml.setContextParam(FlywayMigrationServletContextListener.FLYWAY_JDBC_USER, flywayFraction.jdbcUser());
                webXml.setContextParam(FlywayMigrationServletContextListener.FLYWAY_JDBC_PASSWORD, flywayFraction.jdbcPassword());
            }
        }
    }

    private String getDatasourceNameJndi() {
        String jndiName = "java:jboss/datasources/ExampleDS";
        if (!dsFractionInstance.isUnsatisfied()) {
            List<DataSource> dataSources = dsFractionInstance.get().subresources().dataSources();
            if (dataSources.size() > 0) {
                jndiName = dataSources.get(0).jndiName();
            }
        }
        return jndiName;
    }

}
