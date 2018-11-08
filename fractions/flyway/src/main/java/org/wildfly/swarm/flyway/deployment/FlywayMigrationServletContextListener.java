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
package org.wildfly.swarm.flyway.deployment;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Vetoed;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

/**
 * A ServletContextListener implementation that performs DB migration with
 * Flyway
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Vetoed
@WebListener
public class FlywayMigrationServletContextListener implements ServletContextListener {

    public static final String FLYWAY_JNDI_DATASOURCE = "flyway.jndi.datasource";
    public static final String FLYWAY_JDBC_PASSWORD = "flyway.jdbc.password";
    public static final String FLYWAY_JDBC_USER = "flyway.jdbc.user";
    public static final String FLYWAY_JDBC_URL = "flyway.jdbc.url";

    private static final Logger logger = Logger.getLogger(FlywayMigrationServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        Flyway flyway = new Flyway();
        String dataSourceJndi = sc.getInitParameter(FLYWAY_JNDI_DATASOURCE);
        if (dataSourceJndi != null) {
            try {
                DataSource dataSource = (DataSource) new InitialContext().lookup(dataSourceJndi);
                flyway.setDataSource(dataSource);
            } catch (NamingException ex) {
                logger.log(Level.SEVERE, "Error while looking up DataSource", ex);
                // Do not proceed
                return;
            }
        } else {
            String url = sc.getInitParameter(FLYWAY_JDBC_URL);
            String user = sc.getInitParameter(FLYWAY_JDBC_USER);
            String password = sc.getInitParameter(FLYWAY_JDBC_PASSWORD);
            flyway.setDataSource(url, user, password);
        }
        // Configure with flyway.* system properties
        flyway.configure(System.getProperties());
        flyway.migrate();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
