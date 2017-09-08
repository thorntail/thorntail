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
package org.wildfly.swarm.arquillian.daemon.container;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.wildfly.swarm.arquillian.daemon.protocol.DaemonProtocol;
import org.wildfly.swarm.arquillian.daemon.protocol.DeploymentContext;
import org.wildfly.swarm.arquillian.daemon.protocol.WireProtocol;

/**
 * Base support for containers of the Arquillian Server Daemon
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public abstract class DaemonDeployableContainerBase<CONFIGTYPE extends DaemonContainerConfigurationBase> implements
        DeployableContainer<CONFIGTYPE> {


    @Override
    public void setup(final CONFIGTYPE configuration) {
        final String remoteHost = configuration.getHost();
        final String remotePort = configuration.getPort();
        final InetSocketAddress address = new InetSocketAddress(remoteHost, Integer.parseInt(remotePort));
        this.remoteAddress = address;
        this.javaVmArguments = configuration.getJavaVmArguments();
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void start() throws LifecycleException {
        // Open up remote resources
        try {

            final long startTime = System.currentTimeMillis();
            final int secondsToWait = this.timeout;
            final long acceptableTime = startTime + 1000 * secondsToWait; // 10 seconds from now
            Socket socket = null;
            while (true) {
                try {
                    // TODO Security Action
                    socket = new Socket(remoteAddress.getHostString(), remoteAddress.getPort());
                    if (log.isLoggable(Level.FINEST)) {
                        log.finest("Got connection to " + remoteAddress.toString());
                    }
                    break;
                } catch (final ConnectException ce) {
                    if (log.isLoggable(Level.FINEST)) {
                        log.finest("No connection yet available to remote process");
                    }
                    final long currentTime = System.currentTimeMillis();
                    // Time expired?
                    if (currentTime > acceptableTime) {
                        throw new LifecycleException("Could not connect to the server at "
                                                             + remoteAddress.getHostString() + ":" + remoteAddress.getPort() + " in the allotted "
                                                             + secondsToWait + "s", ce);
                    }
                    // Sleep and try again
                    try {
                        Thread.sleep(200);
                    } catch (final InterruptedException e) {
                        Thread.interrupted();
                        throw new RuntimeException("No one should be interrupting us while we're waiting to connect", e);
                    }
                }
            }
            assert socket != null : "Socket should have been connected";
            this.socket = socket;
            final OutputStream socketOutstream = socket.getOutputStream();
            this.socketOutstream = socketOutstream;
            final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socketOutstream, WireProtocol.CHARSET),
                                                       true);
            this.writer = writer;
            final InputStream socketInstream = socket.getInputStream();
            this.socketInstream = socketInstream;
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socketInstream));
            this.reader = reader;

            final StringBuilder builder = new StringBuilder();
            builder.append(WireProtocol.COMMAND_CHECK_DEPLOYMENT);
            builder.append(WireProtocol.COMMAND_EOF_DELIMITER);
            final String checkCommand = builder.toString();
            // Request
            writer.write(checkCommand);
            writer.flush();

            Throwable error = null;
            try {
                final ObjectInputStream response = new ObjectInputStream(socketInstream);
                error = (Throwable) response.readObject();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (error != null) {
                throw new LifecycleException(error.getMessage(), error);
            }
        } catch (final IOException ioe) {
            this.closeRemoteResources();
            throw new LifecycleException("Could not open connection to remote process", ioe);
        }

    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.container.spi.client.container.DeployableContainer#stop()
     */
    @Override
    public void stop() throws LifecycleException {
        this.closeRemoteResources();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.container.spi.client.container.DeployableContainer#getDefaultProtocol()
     */
    @Override
    public ProtocolDescription getDefaultProtocol() {
        return DaemonProtocol.DESCRIPTION;
    }

    /**
     * @throws UnsupportedOperationException
     * @see org.jboss.arquillian.container.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
     */
    @Override
    public void deploy(final Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException(ERROR_MESSAGE_DESCRIPTORS_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException
     * @see org.jboss.arquillian.container.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
     */
    @Override
    public void undeploy(final Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException(ERROR_MESSAGE_DESCRIPTORS_UNSUPPORTED);

    }

    protected DeploymentContext createDeploymentContext(final String deploymentId) {
        return DeploymentContext.create(deploymentId, socketInstream,
                                        socketOutstream, reader, writer);
    }

    /**
     * @return the remoteAddress
     */
    protected final InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * @return the writer
     */
    protected final PrintWriter getWriter() {
        return writer;
    }

    /**
     * @return the reader
     */
    protected final BufferedReader getReader() {
        return reader;
    }

    /**
     * Safely close remote resources
     */
    private void closeRemoteResources() {
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException ignore) {
            }
            reader = null;
        }
        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (socketOutstream != null) {
            try {
                socketOutstream.close();
            } catch (final IOException ignore) {
            }
            socketOutstream = null;
        }
        if (socketInstream != null) {
            try {
                socketInstream.close();
            } catch (final IOException ignore) {
            }
            socketInstream = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (final IOException ignore) {
            }
            socket = null;
        }
    }

    protected String getJavaVmArguments() {
        return this.javaVmArguments;
    }

    private static final Logger log = Logger.getLogger(DaemonDeployableContainerBase.class.getName());

    private static final String ERROR_MESSAGE_DESCRIPTORS_UNSUPPORTED = "Descriptor deployment not supported";

    private InetSocketAddress remoteAddress;

    private Socket socket;

    private OutputStream socketOutstream;

    private InputStream socketInstream;

    private BufferedReader reader;

    private PrintWriter writer;

    private int timeout = 10;

    private String javaVmArguments;

}
