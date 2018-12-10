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
package org.wildfly.swarm.microprofile.health.runtime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import io.undertow.UndertowLogger;
import io.undertow.UndertowMessages;
import io.undertow.conduits.EmptyStreamSourceConduit;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.HttpUpgradeListener;
import io.undertow.server.SSLSessionInfo;
import io.undertow.server.ServerConnection;
import io.undertow.server.XnioBufferPoolAdaptor;
import io.undertow.util.PooledAdaptor;
import org.jboss.logging.Logger;
import org.xnio.ChannelListener;
import org.xnio.Option;
import org.xnio.OptionMap;
import org.xnio.Pool;
import org.xnio.StreamConnection;
import org.xnio.XnioIoThread;
import org.xnio.XnioWorker;
import org.xnio.channels.Configurable;
import org.xnio.channels.ConnectedChannel;
import org.xnio.conduits.BufferedStreamSinkConduit;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;
import org.xnio.conduits.NullStreamSinkConduit;
import org.xnio.conduits.StreamSinkConduit;

/**
 * An Undertow in vm http connection
 *
 * @author Heiko Braun
 */
final class InVMConnection extends ServerConnection {

    private static Logger LOG = Logger.getLogger(InVMConnection.class);

    private final ByteBufferPool bufferPool;

    private final XnioWorker worker;

    private SSLSessionInfo sslSessionInfo;

    private XnioBufferPoolAdaptor poolAdaptor;

    private BufferingSinkConduit bufferSink;

    protected final List<CloseListener> closeListeners = new LinkedList<>();

    InVMConnection(XnioWorker worker, int port) {
        this.bufferPool = new DefaultByteBufferPool(false, 1024, 0, 0);
        this.worker = worker;
        this.address = new InetSocketAddress(port); // port carried forward from the initial
    }

    public void flushTo(StringBuffer sb) {
        bufferSink.flushTo(sb);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Pool<ByteBuffer> getBufferPool() {
        if (poolAdaptor == null) {
            poolAdaptor = new XnioBufferPoolAdaptor(getByteBufferPool());
        }
        return poolAdaptor;
    }

    @Override
    public ByteBufferPool getByteBufferPool() {
        return bufferPool;
    }

    @Override
    public XnioWorker getWorker() {
        return null;
    }

    @Override
    public XnioIoThread getIoThread() {
        return null;
    }

    @Override
    public HttpServerExchange sendOutOfBandResponse(HttpServerExchange exchange) {
        throw UndertowMessages.MESSAGES.outOfBandResponseNotSupported();
    }

    @Override
    public boolean isContinueResponseSupported() {
        return false;
    }

    @Override
    public void terminateRequestChannel(HttpServerExchange exchange) {
        LOG.trace("Terminate Mock exchange");
    }

    @Override
    public boolean isOpen() {
        return !this.closed;
    }

    @Override
    public boolean supportsOption(Option<?> option) {
        return false;
    }

    @Override
    public <T> T getOption(Option<T> option) throws IOException {
        return null;
    }

    @Override
    public <T> T setOption(Option<T> option, T value) throws IllegalArgumentException, IOException {
        return null;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    @Override
    public SocketAddress getPeerAddress() {
        return null;
    }

    @Override
    public <A extends SocketAddress> A getPeerAddress(Class<A> type) {
        return null;
    }

    @Override
    public ChannelListener.Setter<? extends ConnectedChannel> getCloseSetter() {
        return null;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return address;
    }

    @Override
    public <A extends SocketAddress> A getLocalAddress(Class<A> type) {
        return (A) address;
    }

    @Override
    public OptionMap getUndertowOptions() {
        return OptionMap.EMPTY;
    }

    @Override
    public int getBufferSize() {
        return 1024;
    }

    @Override
    public SSLSessionInfo getSslSessionInfo() {
        return sslSessionInfo;
    }

    @Override
    public void setSslSessionInfo(SSLSessionInfo sessionInfo) {
        sslSessionInfo = sessionInfo;
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        this.closeListeners.add(listener);
    }

    @Override
    public StreamConnection upgradeChannel() {
        return null;
    }

    @Override
    public ConduitStreamSinkChannel getSinkChannel() {

        ConduitStreamSinkChannel sinkChannel = new ConduitStreamSinkChannel(
                Configurable.EMPTY,
                new BufferedStreamSinkConduit(
                        new NullStreamSinkConduit(worker.getIoThread()),
                        new PooledAdaptor(bufferPool.allocate())
                )
        );
        sinkChannel.setCloseListener(conduitStreamSinkChannel -> {
            for (CloseListener l : closeListeners) {
                try {
                    l.closed(InVMConnection.this);
                } catch (Throwable e) {
                    UndertowLogger.REQUEST_LOGGER.exceptionInvokingCloseListener(l, e);
                }
            }
        });
        return sinkChannel;
    }

    @Override
    public ConduitStreamSourceChannel getSourceChannel() {
        return new ConduitStreamSourceChannel(Configurable.EMPTY, new EmptyStreamSourceConduit(worker.getIoThread()));
    }

    @Override
    protected StreamSinkConduit getSinkConduit(HttpServerExchange exchange, StreamSinkConduit conduit) {
        bufferSink = new BufferingSinkConduit(conduit);
        return bufferSink;
    }

    @Override
    protected boolean isUpgradeSupported() {
        return false;
    }

    @Override
    protected boolean isConnectSupported() {
        return false;
    }

    @Override
    protected void exchangeComplete(HttpServerExchange exchange) {
        LOG.trace("InVM exchange complete");
    }

    @Override
    protected void setUpgradeListener(HttpUpgradeListener upgradeListener) {
        //ignore
    }

    @Override
    protected void setConnectListener(HttpUpgradeListener connectListener) {
        //ignore
    }

    @Override
    protected void maxEntitySizeUpdated(HttpServerExchange exchange) {
    }

    @Override
    public String getTransportProtocol() {
        return "mock";
    }

    private boolean closed;

    private final InetSocketAddress address;

    @Override
    public boolean isRequestTrailerFieldsSupported() {
        return false;
    }
}
