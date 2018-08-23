/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.datasources.runtime;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "THORN", length = 4)
public interface DatasourcesMessages extends BasicLogger {

    DatasourcesMessages MESSAGES = Logger.getMessageLogger(DatasourcesMessages.class, "org.wildfly.swarm.datasources");

    int OFFSET = 1000;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = OFFSET + 1, value = "Configuration:\n%s")
    void configuration(String configuration);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = OFFSET + 2, value = "Attempting to auto-detect JDBC driver for %s")
    void attemptToAutoDetectJdbcDriver(String driverName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = OFFSET + 3, value = "Auto-detected JDBC driver for %s")
    void autodetectedJdbcDriver(String driverName);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = OFFSET + 4, value = "Detected JDBC driver %s, but an error occurred while loading it")
    void errorLoadingAutodetectedJdbcDriver(String driverName, @Cause Throwable t);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = OFFSET + 5, value = "Not creating a default datasource due to lack of JDBC driver")
    void notCreatingDatasourceMissingDriver();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = OFFSET + 6, value = "Not creating default datasource %s because one is already defined with that name")
    void notCreatingDatasourceAlreadyExists(String dsName);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = OFFSET + 7, value = "Not creating a default datasource due to ambigous of JDBC drivers: %s")
    void notCreatingDatasourceAmbiguousDrivers(String driverList);


}
