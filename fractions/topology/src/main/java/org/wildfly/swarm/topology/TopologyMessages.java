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
package org.wildfly.swarm.topology;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "TTTOPO", length = 4)
public interface TopologyMessages extends BasicLogger {

    TopologyMessages MESSAGES = Logger.getMessageLogger(TopologyMessages.class, "org.wildfly.swarm.topology");

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 1, value = "Error firing topology event on %s.")
    void errorFiringEvent(String listenerClass, @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 2, value = "Client not registered: %s.")
    void notRegistered(String clientId, @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 3, value = "Error starting advertisement.")
    void errorStartingAdvertisement(@Cause Throwable cause);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 4, value = "Error stopping advertisement.")
    void errorStoppingAdvertisement(@Cause Throwable cause);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 5, value = "Error sending check for %s.")
    void errorOnCheck(String clientId, @Cause Throwable cause);
}
