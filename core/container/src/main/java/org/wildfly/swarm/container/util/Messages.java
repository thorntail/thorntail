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
package org.wildfly.swarm.container.util;

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
public interface Messages extends BasicLogger {

    Messages MESSAGES = Logger.getMessageLogger(Messages.class, "org.wildfly.swarm");

    int OFFSET = 1120;

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = OFFSET + 1, value = "Attempting to auto-detect driver for %s")
    void attemptToAutoDetectDriver(String driverName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = OFFSET + 2, value = "Auto-detected driver for %s")
    void autodetectedDriver(String driverName);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = OFFSET + 3, value = "Detected driver %s, but an error occurred while loading it")
    void errorLoadingAutodetectedDriver(String driverName, @Cause Throwable t);

    @Message(id = OFFSET + 4, value = "Cannot generate new module '%s' " +
                "when module '%s' is already being used by application")
    IllegalStateException cannotAddReferenceToModule(String module, String alreadyAssociatedModule);

}
