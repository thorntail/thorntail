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
package org.wildfly.swarm.flyway;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * Flyway Fraction
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@DeploymentModule(name = "org.wildfly.swarm.flyway", slot = "deployment")
@Configurable("thorntail.flyway")
public class FlywayFraction implements Fraction<FlywayFraction> {

    /**
     * Uses the specified connection info if not <code>null</code>. Otherwise
     * use primary Datasource
     */
    @AttributeDocumentation("JDBC connection URL")
    private String jdbcUrl;
    @AttributeDocumentation("JDBC connection user name")
    private String jdbcUser;
    @AttributeDocumentation("JDBC connection password")
    private String jdbcPassword;

    public String jdbcPassword() {
        return jdbcPassword;
    }

    public String jdbcUrl() {
        return jdbcUrl;
    }

    public String jdbcUser() {
        return jdbcUser;
    }

    public FlywayFraction jdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
        return this;
    }

    public FlywayFraction jdbcUser(String jdbcUser) {
        this.jdbcUser = jdbcUser;
        return this;
    }

    public FlywayFraction jdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public boolean usePrimaryDataSource() {
        return this.jdbcPassword == null;
    }
}
