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
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.wildfly.swarm.container.DeploymentException;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "TTDEPLOY", length = 4)
public interface DeployerMessages extends BasicLogger {

    DeployerMessages MESSAGES = Logger.getMessageLogger(DeployerMessages.class, "org.wildfly.swarm.deployer");

    @Message(id = 1, value = "Unable to create default deployment.")
    DeploymentException unableToCreateDefaultDeployment();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 2, value = "No deployments specified")
    void noDeploymentsSpecified();

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 3, value = "Deploying %s")
    void deploying(String deploymentName);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 4, value = "Deployment content: %s")
    void deploymentContent(String path);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 5, value = "Exporting deployment to %s")
    void exportingDeployment(String exportLocation);

    @Message(id = 6, value = "Unable to create default deployment of type .war, maybe missing the 'undertow' or 'jaxrs' fraction")
    DeploymentException unableToCreateDefaultDeploymentWar();


}
