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
package org.wildfly.swarm.internal;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "TTCONFIG", length = 4)
public interface SwarmConfigMessages extends BasicLogger {

    SwarmConfigMessages MESSAGES = Logger.getMessageLogger(SwarmConfigMessages.class, "org.wildfly.swarm.config");

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 1, value = "Marshalling Project Stage property %s")
    void marshalProjectStageProperty(String key);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2, value = "Marshalling XML from %s as: \n %s")
    void marshalXml(String location, String xml);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 3, value = "Load standalone.xml via %s from %s")
    void loadingStandaloneXml(String loader, String location);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 4, value = "Configuration:\n%s")
    void configuration(String configuration);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 5, value = "Error resolving configurable value for %s.")
    void errorResolvingConfigurableValue(String key, @Cause Throwable cause);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 6, value = "Error loading module.")
    void errorLoadingModule(@Cause Throwable cause);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 7, value = "Error create extension %s from module %s.")
    void errorCreatingExtension(String extensionClassName, String extensionModuleName, @Cause Throwable cause);

}
