/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.arquillian.daemon.protocol;

import java.util.Collection;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;

/**
 * {@link Protocol} implementation for the Arquillian Server Daemon
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class DaemonProtocol implements Protocol<DaemonProtocolConfiguration> {

    public static final ProtocolDescription DESCRIPTION = new ProtocolDescription(DaemonProtocol.class.getSimpleName());

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.container.test.spi.client.protocol.Protocol#getProtocolConfigurationClass()
     */
    @Override
    public Class<DaemonProtocolConfiguration> getProtocolConfigurationClass() {
        return DaemonProtocolConfiguration.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.container.test.spi.client.protocol.Protocol#getDescription()
     */
    @Override
    public ProtocolDescription getDescription() {
        return DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.container.test.spi.client.protocol.Protocol#getPackager()
     */
    @Override
    public DeploymentPackager getPackager() {
        return DaemonDeploymentPackager.INSTANCE;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.container.test.spi.client.protocol.Protocol#getExecutor(org.jboss.arquillian.container.test.spi.client.protocol.ProtocolConfiguration,
     *      org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData,
     *      org.jboss.arquillian.container.test.spi.command.CommandCallback)
     */
    @Override
    public ContainerMethodExecutor getExecutor(final DaemonProtocolConfiguration protocolConfiguration,
        final ProtocolMetaData metaData, final CommandCallback callback) {
        final Collection<DeploymentContext> contexts = metaData.getContexts(DeploymentContext.class);
        assert contexts.size() == 1 : "Should be exactly one deployment context";
        final DeploymentContext context = contexts.iterator().next();
        return new DaemonMethodExecutor(context);
    }

}
