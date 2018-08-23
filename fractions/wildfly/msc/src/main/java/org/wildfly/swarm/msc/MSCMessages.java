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
package org.wildfly.swarm.msc;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author Bob McWhirter
 */
@MessageLogger(projectCode = "TTMSC", length = 4)
public interface MSCMessages extends BasicLogger {
    MSCMessages MESSAGES = Logger.getMessageLogger(MSCMessages.class, "org.wildfly.swarm.msc");

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 1, value = "Error reading ServiceActivator asset")
    void errorReadingServiceActivatorAsset(@Cause Throwable cause);
}
