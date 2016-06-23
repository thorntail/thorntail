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
package org.wildfly.swarm.remoting;

import org.wildfly.swarm.config.Remoting;
import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.remoting.EndpointConfiguration;
import org.wildfly.swarm.config.remoting.HTTPConnector;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.server.HTTPListener;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.annotations.Configuration;
import org.wildfly.swarm.spi.api.annotations.Default;

/**
 * @author Ken Finnigan
 */
@Configuration(
        marshal = true,
        extension = "org.jboss.as.remoting"
)
public class RemotingFraction extends Remoting<RemotingFraction> implements Fraction {

    @Default
    public static RemotingFraction defaultFraction() {
        RemotingFraction fraction = new RemotingFraction();
        fraction.endpointConfiguration(new EndpointConfiguration())
                .httpConnector(new HTTPConnector("http-remoting-connector")
                        .connectorRef("default") );
        return fraction;
    }

    @Override
    public void postInitialize(PostInitContext initContext) {
        System.setProperty(SwarmProperties.HTTP_EAGER, "true" );
        Undertow undertow = (Undertow) initContext.fraction( "undertow" );

        if ( undertow != null ) {
            Server server = undertow.subresources().server("default-server");
            if ( server != null ) {
                HTTPListener listener = server.subresources().httpListener("default");
                if ( listener != null ) {
                    listener.enabled(true);
                }
            }
        }

    }
}
