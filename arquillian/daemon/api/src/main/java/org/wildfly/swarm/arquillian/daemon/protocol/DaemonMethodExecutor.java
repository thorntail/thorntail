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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;

/**
 * {@link ContainerMethodExecutor} implementation which executes tests on the remote JVM Arquillian Server Daemon and
 * returns the {@link TestResult} it returns.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class DaemonMethodExecutor implements ContainerMethodExecutor {

    DaemonMethodExecutor(final DeploymentContext context) {
        if (context == null) {
            throw new IllegalArgumentException("deployment context must be specified");
        }
        this.context = context;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.container.test.spi.ContainerMethodExecutor#invoke(org.jboss.arquillian.test.spi.TestMethodExecutor)
     */
    @Override
    public TestResult invoke(final TestMethodExecutor testMethodExecutor) {

        assert testMethodExecutor != null : "Test method executor is required";

        // Build the String request according to the wire protocol
        final StringBuilder builder = new StringBuilder();
        builder.append(WireProtocol.COMMAND_TEST_PREFIX);
        builder.append(testMethodExecutor.getInstance().getClass().getName());
        builder.append(SPACE);
        builder.append(testMethodExecutor.getMethod().getName());
        builder.append(WireProtocol.COMMAND_EOF_DELIMITER);
        final String testCommand = builder.toString();
        final PrintWriter writer = this.context.getWriter();

        // Request
        writer.write(testCommand);
        writer.flush();

        try {
            // Read response
            final ObjectInputStream response = new ObjectInputStream(
                    new NoCloseInputStream(context.getSocketInstream()));
            final TestResult testResult = (TestResult) response.readObject();
            response.close();
            return testResult;

        } catch (final IOException ioe) {
            throw new RuntimeException("Could not get test results", ioe);
        } catch (final ClassNotFoundException cnfe) {
            throw new RuntimeException("test result not on the client classpath", cnfe);
        }
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(DaemonMethodExecutor.class.getName());

    private static final String SPACE = " ";

    private final DeploymentContext context;

    /**
     * Wrapper which does forwards all operations except {@link InputStream#close()} to the delegate
     *
     * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
     */
    private static final class NoCloseInputStream extends InputStream {

        NoCloseInputStream(final InputStream delegate) {
            assert delegate != null : "delegate must be specified";
            this.delegate = delegate;
        }

        /**
         * Do not propagate {@link InputStream#close()}
         *
         * @see java.io.InputStream#close()
         */
        @Override
        public void close() throws IOException {
            // NOOP
        }

        /**
         * @return
         * @throws IOException
         * @see java.io.InputStream#read()
         */
        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        /**
         * @return
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        /**
         * @param b
         * @return
         * @throws IOException
         * @see java.io.InputStream#read(byte[])
         */
        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        /**
         * @param obj
         * @return
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        /**
         * @param b
         * @param off
         * @param len
         * @return
         * @throws IOException
         * @see java.io.InputStream#read(byte[], int, int)
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        /**
         * @param n
         * @return
         * @throws IOException
         * @see java.io.InputStream#skip(long)
         */
        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        /**
         * @return
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return delegate.toString();
        }

        /**
         * @return
         * @throws IOException
         * @see java.io.InputStream#available()
         */
        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        /**
         * @param readlimit
         * @see java.io.InputStream#mark(int)
         */
        @Override
        public void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        /**
         * @throws IOException
         * @see java.io.InputStream#reset()
         */
        @Override
        public void reset() throws IOException {
            delegate.reset();
        }

        /**
         * @return
         * @see java.io.InputStream#markSupported()
         */
        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        private final InputStream delegate;

    }

}
