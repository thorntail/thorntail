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
package org.wildfly.swarm.arquillian.daemon.protocol;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.jboss.arquillian.container.spi.client.protocol.metadata.NamedContext;

/**
 * {@link NamedContext} implementation backed by streams and reader/writer to interact with the Arquillian Server Daemon
 * over wire protocol. No caller should close any of the resources in this {@link DeploymentContext}; they are to be
 * managed by the establishing container. Essentially acts as a value object to hand off resources between the container
 * and the {@link DaemonMethodExecutor}
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class DeploymentContext extends NamedContext {

    private final InputStream socketInstream;

    private final OutputStream socketOutstream;

    private final BufferedReader reader;

    private final PrintWriter writer;

    private DeploymentContext(final String deploymentName, final InputStream socketInstream,
                              final OutputStream socketOutstream, final BufferedReader reader, final PrintWriter writer) {
        super(deploymentName);
        this.socketInstream = socketInstream;
        this.socketOutstream = socketOutstream;
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * Creates and returns a new {@link DeploymentContext} instance from the required arguments
     *
     * @param deploymentName  Name of the deployment
     * @param socketInstream
     * @param socketOutstream
     * @param reader
     * @param writer
     * @return
     * @throws IllegalArgumentException If any argument is not specified
     */
    public static DeploymentContext create(final String deploymentName, final InputStream socketInstream,
                                           final OutputStream socketOutstream, final BufferedReader reader, final PrintWriter writer)
            throws IllegalArgumentException {
        if (deploymentName == null || deploymentName.length() == 0) {
            throw new IllegalArgumentException("Deployment name must be specified");
        }
        if (socketInstream == null) {
            throw new IllegalArgumentException("socket instream must be specified");
        }
        if (socketOutstream == null) {
            throw new IllegalArgumentException("socket outstream must be specified");
        }
        if (reader == null) {
            throw new IllegalArgumentException("reader must be specified");
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer must be specified");
        }
        final DeploymentContext context = new DeploymentContext(deploymentName, socketInstream, socketOutstream,
                reader, writer);
        return context;
    }

    /**
     * @return the socketInstream
     */
    public InputStream getSocketInstream() {
        return socketInstream;
    }

    /**
     * @return the socketOutstream
     */
    public OutputStream getSocketOutstream() {
        return socketOutstream;
    }

    /**
     * @return the reader
     */
    public BufferedReader getReader() {
        return reader;
    }

    /**
     * @return the writer
     */
    public PrintWriter getWriter() {
        return writer;
    }

}
