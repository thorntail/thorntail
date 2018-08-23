/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.topology.consul;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "TTCNSL", length = 4)
public interface ConsulTopologyMessages extends BasicLogger {

    ConsulTopologyMessages MESSAGES = Logger.getMessageLogger(ConsulTopologyMessages.class, "org.wildfly.swarm.topology.consul");

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 1, value = "Error stopping catalog watcher for: %s.")
    void errorStoppingCatalogWatcher(String key, @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 2, value = "Error stopping catalog watcher for: %s.")
    void errorSettingUpCatalogWatcher(String key, @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 3, value = "Error while querying service data from consul.")
    void errorOnCatalogUpdate(@Cause Throwable t);

}
