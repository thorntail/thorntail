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

import java.util.Collection;
import java.util.List;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Param;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "WFSWARM", length = 4)
public interface SwarmMessages extends BasicLogger {

    SwarmMessages MESSAGES = Logger.getMessageLogger(SwarmMessages.class, "org.wildfly.swarm");

    @Message(id = 2, value = "Cannot invoke %s on a container that has not been started.")
    IllegalStateException containerNotStarted(String method);

    @Message(id = 4, value = "%s requires an argument.")
    RuntimeException argumentRequired(String arg);

    @Message(id = 5, value = "Unable to create default deployment")
    DeploymentException cannotCreateDefaultDeployment();

    @Message(id = 6, value = "Failed to mount deployment.")
    DeploymentException failToMountDeployment(@Cause Throwable cause, @Param Archive<?> archive);

    @Message(id = 7, value = "Deployment failed: %s")
    String deploymentFailed(String failureMessage);

    @Message(id = 8, value = "Failure during deployment")
    DeploymentException deploymentFailed(@Cause Throwable cause, @Param Archive<?> archive);

    @Message(id = 9, value = "JavaArchive spec does not support Libraries")
    UnsupportedOperationException librariesNotSupported();

    @Message(id = 12, value = "Fraction \"%s\" was configured using @WildFlyExtension with a module='',"
            + " but has multiple extension classes.  Please use classname='' to specify exactly one, or noClass=true to ignore all. %s")
    RuntimeException fractionHasMultipleExtensions(String className, Collection<String> extensions);

    @Message(id = 13, value = "Artifact '%s' not found.")
    RuntimeException artifactNotFound(String gav);

    @Message(id = 14, value = "Unable to determine version number from GAV: %s")
    RuntimeException unableToDetermineVersion(String gav);

    @Message(id = 15, value = "GAV must includes at least 2 segments")
    RuntimeException gavMinimumSegments();

    @Message(id = 16, value = "System property '%s' not provided.")
    IllegalStateException systemPropertyNotFound(String key);

    @Message(id = 17, value = "Cannot identify FileSystemLayout for given path: %s")
    IllegalArgumentException cannotIdentifyFileSystemLayout(String path);

    @Message(id = 18, value = "Installed fraction: %24s - %-15s %s:%s:%s")
    String availableFraction(String name, String stabilityLevel, String groupId, String artifactId, String version);

    @Message(id = 19, value = "No deployments specified on the command-line")
    String noDeploymentsSpecified();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 23, value = "Unable to setup Shrinkwrap Domain")
    void shrinkwrapDomainSetupFailed(@Cause Throwable cause);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 25, value = "Add deployment module: %s")
    void deploymentModuleAdded(DeploymentModule module);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 26, value = "Calling Pre Customizer: %s")
    void callingPreCustomizer(Customizer customizer);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 27, value = "Calling Post Customizer: %s")
    void callingPostCustomizer(Customizer customizer);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 28, value = "WildFly Bootstrap operations: \n %s")
    void wildflyBootstrap(String operations);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 29, value = "Install MSC service for command line args: %s")
    void argsInstalled(List<String> args);

    @Message(id = 33, value = "HTTP/S is configured correctly, but org.wildfly.swarm:management is not available")
    RuntimeException httpsRequiresManagementFraction();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 34, value = "Ignoring subsystem %s:%s")
    void ignoringSubsystem(String nsURI, String name);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 35, value = "Failed to register modules mbeans")
    void moduleMBeanServerNotInstalled(@Cause Throwable cause);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 36, value = "Error installing user-space CDI extension: %s")
    void errorInstallingUserSpaceExtension(String factoryClassName);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 38, value =
            "In order to use HTTP/2 in WildFly Swarm, you must have the OpenSSL provider with ALPN capability from " +
                    "JBoss Core Services installed and configured. This is due to the fact that HTTP/2 requires " +
                    "a TLS stack that supports ALPN, which is not provided by the default installation of Java 8. " +
                    "HTTP/2 will only work with browsers that also support the HTTP/2 standard. " +
                    "OpenSSL usage with WildFly Swarm on HP-UX is NOT supported.")
    void http2NotSupported();


    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 99999, value = "WildFly Swarm is Ready")
    void wildflySwarmIsReady();


}
