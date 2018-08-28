/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.infinispan;


import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 3/15/18
 */
@MessageLogger(projectCode = "TTISPN", length = 4)
public interface InfinispanMessages extends BasicLogger {

    InfinispanMessages MESSAGES = Logger.getMessageLogger(InfinispanMessages.class, "org.wildfly.swarm.infinispan");

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 1, value = "Skipping activating cache configuration service: %s. " +
            "The corresponding fraction is not present.")
    void skippingCacheActivation(String key);
}
