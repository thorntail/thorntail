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
package org.wildfly.swarm.netflix.ribbon.secured.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.netflix.client.RetryHandler;
import com.netflix.client.config.ClientConfigFactory;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.reactive.ExecutionListener;
import com.netflix.ribbon.RibbonTransportFactory;
import com.netflix.ribbon.transport.netty.RibbonTransport;
import com.netflix.ribbon.transport.netty.http.LoadBalancingHttpClient;
import com.netflix.ribbon.transport.netty.http.NettyHttpLoadBalancerErrorHandler;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.pipeline.PipelineConfiguratorComposite;
import io.reactivex.netty.protocol.http.HttpObjectAggregationConfigurator;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientPipelineConfigurator;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;

/**
 * @author Bob McWhirter
 */
public class SecuredTransportFactory extends RibbonTransportFactory {

    public static final ScheduledExecutorService poolCleanerScheduler;

    protected SecuredTransportFactory(final int maxChunkSize) {
        super(ClientConfigFactory.DEFAULT);
        this.maxChunkSize = maxChunkSize;
    }

    @Override
    public HttpClient<ByteBuf, ByteBuf> newHttpClient(final IClientConfig config) {
        final List<ExecutionListener<HttpClientRequest<ByteBuf>, HttpClientResponse<ByteBuf>>> listeners = new ArrayList<>();
        listeners.add(createBearerHeaderAdder());
        final PipelineConfiguratorComposite<HttpClientResponse<ByteBuf>, HttpClientRequest<ByteBuf>> pipelineConfigurator = new PipelineConfiguratorComposite<HttpClientResponse<ByteBuf>,
                HttpClientRequest<ByteBuf>>(new HttpClientPipelineConfigurator<ByteBuf, ByteBuf>(),
                                            new HttpObjectAggregationConfigurator(maxChunkSize));
        final LoadBalancingHttpClient<ByteBuf, ByteBuf> client = LoadBalancingHttpClient.<ByteBuf, ByteBuf>builder()
                .withClientConfig(config)
                .withExecutorListeners(listeners)
                .withRetryHandler(getDefaultHttpRetryHandlerWithConfig(config))
                .withPipelineConfigurator(pipelineConfigurator)
                .withPoolCleanerScheduler(RibbonTransport.poolCleanerScheduler)
                .build();

        return client;
    }

    private static RetryHandler getDefaultHttpRetryHandlerWithConfig(final IClientConfig config) {
        return new NettyHttpLoadBalancerErrorHandler(config);
    }

    private ExecutionListener<HttpClientRequest<ByteBuf>, HttpClientResponse<ByteBuf>> createBearerHeaderAdder() {
        return new BearerHeaderAdder();
    }

    static {
        poolCleanerScheduler = Executors.newScheduledThreadPool(1);
    }

    private final int maxChunkSize;

}
