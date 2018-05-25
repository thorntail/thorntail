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
package org.wildfly.swarm.arquillian.daemon.server;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.shrinkwrap.api.ConfigurationBuilder;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.arquillian.daemon.TestRunner;
import org.wildfly.swarm.arquillian.daemon.protocol.WireProtocol;

/**
 * Netty-based implementation of a server; not thread-safe via the Java API (though invoking wire protocol
 * operations through its communication channels is). Responsible for handling I/O aspects of the server daemon.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 * @author Toby Crawley
 */
public class Server {

    public static final int MAX_PORT = 65535;

    private DeploymentUnit deploymentUnit;


    Server(final InetSocketAddress bindAddress) {
        // Precondition checks
        assert bindAddress != null : "Bind address must be specified";

        // Determine the ClassLoader to use in creating the SW Domain
        final ClassLoader thisCl = Server.class.getClassLoader();
        final Set<ClassLoader> classloaders = new HashSet<>(1);
        classloaders.add(thisCl);
        if (Server.log.isLoggable(Level.FINEST)) {
            Server.log.finest("Using ClassLoader for ShrinkWrap Domain: " + thisCl);
        }
        this.shrinkwrapDomain = ShrinkWrap.createDomain(new ConfigurationBuilder().classLoaders(classloaders));

        // Set
        this.bindAddress = bindAddress;
    }

    public static Server create(final String bindAddress, final int bindPort) throws IllegalArgumentException {

        // Precondition checks
        if (bindPort < 0 || bindPort > MAX_PORT) {
            throw new IllegalArgumentException("Bind port must be between 0 and " + MAX_PORT);
        }

        // Create the inetaddress and ensure it's resolved
        final InetSocketAddress resolvedInetAddress = bindAddress == null ? new InetSocketAddress(bindPort)
                : new InetSocketAddress(bindAddress, bindPort);
        if (resolvedInetAddress.isUnresolved()) {
            throw new IllegalArgumentException("Address \"" + bindAddress + "\" could not be resolved");
        }

        // Create and return a new server instance
        return new Server(resolvedInetAddress);
    }

    public final void start() throws ServerLifecycleException, IllegalStateException {

        // Precondition checks
        if (this.isRunning()) {
            throw new IllegalStateException("Already running");
        }

        // Set up Netty Boostrap
        final EventLoopGroup parentGroup = new NioEventLoopGroup();
        final EventLoopGroup childGroup = new NioEventLoopGroup();
        this.eventLoopGroups.add(parentGroup);
        this.eventLoopGroups.add(childGroup);
        final ServerBootstrap bootstrap = new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(this.getBindAddress())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(final SocketChannel channel) throws Exception {
                        final ChannelPipeline pipeline = channel.pipeline();
                        setupPipeline(pipeline);
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Start 'er up
        final ChannelFuture openChannel;
        try {
            openChannel = bootstrap.bind().sync();
        } catch (final InterruptedException ie) {
            Thread.interrupted();
            throw new ServerLifecycleException("Interrupted while awaiting server start", ie);
        } catch (final RuntimeException re) {
            // Exception xlate
            throw new ServerLifecycleException("Encountered error in binding; could not start server.", re);
        }
        // Set bound address
        final InetSocketAddress boundAddress = ((InetSocketAddress) openChannel.channel().localAddress());

        // Running
        running = true;
        // Create the shutdown service
        this.shutdownService = Executors.newSingleThreadExecutor();

        if (log.isLoggable(Level.INFO)) {
            log.info("Arquillian Daemon server started on " + boundAddress.getHostName() + ":" +
                             boundAddress.getPort());
        }

    }

    public final synchronized void stop() throws ServerLifecycleException, IllegalStateException, InterruptedException {
        // Use an anonymous logger because the JUL LogManager will not log after process shutdown has been received
        final Logger log = Logger.getAnonymousLogger();
        log.addHandler(new Handler() {

            @Override
            public void publish(final LogRecord record) {
                System.out.println(PREFIX + record.getMessage());
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {
            }

            private final String PREFIX = "[" + Server.class.getSimpleName() + "] ";
        });

        if (!this.isRunning()) {
            throw new IllegalStateException("Server is not running");
        }

        if (log.isLoggable(Level.INFO)) {
            log.info("Requesting shutdown...");
        }

        this.eventLoopGroups.forEach(EventLoopGroup::shutdownGracefully);
        this.eventLoopGroups.clear();

        shutdownService.shutdown();
        if (!shutdownService.awaitTermination(2, TimeUnit.MINUTES)) {
            log.warning("Unable to shutdown the server process cleanly.");
        }
        // Kill the shutdown service
        shutdownService.shutdownNow();
        shutdownService = null;

        // Not running
        running = false;

        if (log.isLoggable(Level.INFO)) {
            log.info("Server shutdown.");
        }
    }

    public final boolean isRunning() {
        return running;
    }

    private static ChannelFuture sendResponse(final ChannelHandlerContext ctx, final String response) {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(response.getBytes(WireProtocol.CHARSET));
        ctx.write(buf);

        return ctx.writeAndFlush(Delimiters.lineDelimiter()[0]);
    }

    /**
     * The address configured to which we should bind
     *
     * @return
     */
    protected final InetSocketAddress getBindAddress() {
        return this.bindAddress;
    }

    /**
     * @return the deployedArchives
     */
    protected final ConcurrentMap<String, GenericArchive> getDeployedArchives() {
        return deployedArchives;
    }

    /**
     * @return the shrinkwrapDomain
     */
    protected final Domain getShrinkwrapDomain() {
        return shrinkwrapDomain;
    }

    public void setDeploymentUnit(DeploymentUnit deploymentUnit) {
        this.deploymentUnit = deploymentUnit;
    }

    public void setError(Throwable error) {
        this.error = error;
    }


    protected final Serializable executeTest(final String testClassName, final String methodName) {
        return new TestRunner(deploymentUnit).executeTest(testClassName, methodName);
    }

    protected Serializable checkDeployment() {
        return this.error;
    }

    /**
     * Asynchronously calls upon {@link Server#stop()}
     */
    protected final void stopAsync() {

        shutdownService.submit(() -> {
            Server.this.stop();
            return null;
        });
    }

    private void setupPipeline(final ChannelPipeline pipeline) {
        pipeline.addLast(NAME_CHANNEL_HANDLER_FRAME_DECODER,
                         new DelimiterBasedFrameDecoder(2000, Delimiters.lineDelimiter()));
        pipeline.addLast(NAME_CHANNEL_HANDLER_STRING_DECODER,
                         new StringDecoder(WireProtocol.CHARSET));
        pipeline.addLast(NAME_CHANNEL_HANDLER_COMMAND, new StringCommandHandler());
    }

    private static final Logger log = Logger.getLogger(Server.class.getName());

    private static final String NAME_CHANNEL_HANDLER_STRING_DECODER = "StringDecoder";

    private static final String NAME_CHANNEL_HANDLER_FRAME_DECODER = "FrameDecoder";

    private static final String NAME_CHANNEL_HANDLER_COMMAND = "CommandHandler";

    private final List<EventLoopGroup> eventLoopGroups = new ArrayList<>();

    private final ConcurrentMap<String, GenericArchive> deployedArchives = new ConcurrentHashMap<>();

    private final Domain shrinkwrapDomain;

    private final InetSocketAddress bindAddress;

    private ExecutorService shutdownService;

    private boolean running;

    private Throwable error;

    /**
     * Handler for all {@link String}-based commands to the server as specified in {@link WireProtocol}
     *
     * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
     */
    private class StringCommandHandler extends SimpleChannelInboundHandler<String> {

        /**
         * Ignores all exceptions on messages received if the server is not running, else delegates to the super
         * implementation.
         *
         * @see io.netty.channel.SimpleChannelInboundHandler#exceptionCaught(io.netty.channel.ChannelHandlerContext,
         * java.lang.Throwable)
         */
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            // If the server isn't running, ignore everything
            if (!Server.this.isRunning()) {
                // Ignore, but log if we've got a fine-grained enough level set
                if (log.isLoggable(Level.FINEST)) {
                    log.finest("Got exception while server is not running: " + cause.getMessage());
                }
                ctx.close();
            } else {
                super.exceptionCaught(ctx, cause);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(ChannelHandlerContext, Object)
         */
        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final String message) throws Exception {
            // We want to catch any and all errors to to write out a proper response to the client
            try {
                // Stop
                if (WireProtocol.COMMAND_CHECK_DEPLOYMENT.equals(message)) {
                    Serializable error = Server.this.checkDeployment();
                    ObjectOutputStream objectOutstream = null;
                    ByteBuf out = ctx.alloc().buffer();
                    objectOutstream = new ObjectOutputStream(new ByteBufOutputStream(out));
                    objectOutstream.writeObject(error);
                    objectOutstream.flush();
                    ctx.writeAndFlush(out);
                } else if (WireProtocol.COMMAND_STOP.equals(message)) {

                    // Set the response to tell the client OK
                    Server.sendResponse(ctx, WireProtocol.RESPONSE_OK_PREFIX + message)
                            .addListener(future -> Server.this.stopAsync());
                } else if (message.startsWith(WireProtocol.COMMAND_TEST_PREFIX)) {
                    // Test

                    // Parse out the arguments
                    final StringTokenizer tokenizer = new StringTokenizer(message);
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    final String testClassName = tokenizer.nextToken();

                    final String methodName = tokenizer.nextToken();

                    // Execute the test and get the result
                    final Serializable testResult = Server.this.executeTest(testClassName, methodName);

                    ObjectOutputStream objectOutstream = null;
                    try {
                        // Write the test result
                        ByteBuf out = ctx.alloc().buffer();
                        objectOutstream = new ObjectOutputStream(new ByteBufOutputStream(out));
                        objectOutstream.writeObject(testResult);
                        objectOutstream.flush();
                        ctx.writeAndFlush(out);
                    } finally {
                        if (objectOutstream != null) {
                            objectOutstream.close();
                        }
                    }
                } else {
                    // Unsupported command
                    throw new UnsupportedOperationException("This server does not support command: " + message);
                }

            } catch (final Throwable t) {
                // Will be captured by any remote process which launched us and is piping in our output
                t.printStackTrace();
                Server.sendResponse(ctx, WireProtocol.RESPONSE_ERROR_PREFIX
                        + "Caught unexpected error servicing request: " + t.getMessage());
            }

        }

    }
}
