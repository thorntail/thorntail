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

package org.wildfly.swarm.internal;

import java.net.URL;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "WFSWARM", length = 4)
public interface SwarmConfigMessages extends BasicLogger {

    SwarmConfigMessages MESSAGES = Logger.getMessageLogger(SwarmConfigMessages.class, "org.wildfly.swarm.config");

    @Message(id = 1, value = "Stage config is not present.")
    RuntimeException missingStageConfig();

    @Message(id = 3, value = "Project stage '%s' cannot be found.")
    RuntimeException stageNotFound(String stageName);

    @Message(id = 10, value = "Failed to load stage configuration from URL : %s")
    RuntimeException failedLoadingStageConfig(@Cause Throwable cause, URL url);

    @Message(id = 11, value = "Missing stage 'default' in project-stages.yml")
    RuntimeException missingDefaultStage();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 20, value = "Stage Config found in %s at location: %s")
    void stageConfigLocation(String configType, String configLocation);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 21, value = "Failed to parse project stage URL reference, ignoring: %s")
    void malformedStageConfigUrl(String error);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 22, value = "Project stage superseded by external configuration %s")
    void stageConfigSuperseded(String location);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 24, value = "Using project stage: %s")
    void usingProjectStage(String stageName);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 30, value = "Marshalling Project Stage property %s")
    void marshalProjectStageProperty(String key);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 31, value = "Marshalling XML from %s as: \n %s")
    void marshalXml(String location, String xml);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 32, value = "Load standalone.xml via %s from %s")
    void loadingStandaloneXml(String loader, String location);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 37, value = "Configuration:\n%s")
    void configuration(String configuration);

}
