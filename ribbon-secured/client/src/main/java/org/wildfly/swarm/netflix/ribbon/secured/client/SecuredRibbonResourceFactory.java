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

import com.netflix.client.config.ClientConfigFactory;
import com.netflix.ribbon.RibbonResourceFactory;
import com.netflix.ribbon.RibbonTransportFactory;
import com.netflix.ribbon.proxy.processor.AnnotationProcessorsProvider;
import io.reactivex.netty.protocol.http.HttpObjectAggregationConfigurator;

/**
 * @author Bob McWhirter
 */
public class SecuredRibbonResourceFactory extends RibbonResourceFactory {

    public static SecuredRibbonResourceFactory INSTANCE = new SecuredRibbonResourceFactory(HttpObjectAggregationConfigurator.DEFAULT_CHUNK_SIZE);

    public SecuredRibbonResourceFactory(final int maxChunkSize) {
        this(ClientConfigFactory.DEFAULT,
             new SecuredTransportFactory(maxChunkSize),
             AnnotationProcessorsProvider.DEFAULT);
    }

    public SecuredRibbonResourceFactory(final ClientConfigFactory configFactory, final RibbonTransportFactory transportFactory, final AnnotationProcessorsProvider processors) {
        super(configFactory, transportFactory, processors);
    }


}
