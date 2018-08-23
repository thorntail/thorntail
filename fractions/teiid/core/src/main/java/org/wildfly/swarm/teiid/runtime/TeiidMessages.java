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
package org.wildfly.swarm.teiid.runtime;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "TTTEIID", length = 4)
public interface TeiidMessages extends BasicLogger {

    TeiidMessages MESSAGES = Logger.getMessageLogger(TeiidMessages.class, "org.wildfly.swarm.teiid");

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 26000, value = "Auto Detected Teiid Translator:%s")
    void autoDetectedTranslator(String name);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 26001, value = "Detected Translator %s, but an error occurred while loading it")
    void errorLoadingAutodetectedTranslator(String driverName, @Cause Throwable t);
}
