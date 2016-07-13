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
package org.wildfly.swarm.webservices;

import org.wildfly.swarm.config.Webservices;
import org.wildfly.swarm.config.webservices.ClientConfig;
import org.wildfly.swarm.config.webservices.EndpointConfig;
import org.wildfly.swarm.config.webservices.Handler;
import org.wildfly.swarm.config.webservices.PreHandlerChain;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.annotations.Default;
import org.wildfly.swarm.spi.api.annotations.ExtensionModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;

@ExtensionModule("org.jboss.as.webservices")
@MarshalDMR
public class WebServicesFraction extends Webservices<WebServicesFraction> implements Fraction {

    private WebServicesFraction() {

    }

    @Default
    public static WebServicesFraction createDefaultFraction() {

        String SoapHost = System.getProperty(SwarmProperties.BIND_ADDRESS, SOAP_HOST);

        return new WebServicesFraction()
                .wsdlHost(SoapHost)
                .endpointConfig(new EndpointConfig(STANDARD_ENDPOINT_CONFIG))
                .endpointConfig(createRemoteEndpoint())
                .clientConfig(new ClientConfig(STANDARD_CLIENT_CONFIG));
    }

    private static final EndpointConfig createRemoteEndpoint() {
        return new EndpointConfig(RECORDING)
                .preHandlerChain(new PreHandlerChain(RECORDING_HANDLERS)
                                         .protocolBindings(SOAP_PROTOCOLS)
                                         .handler(new Handler(RECORDING_HANDLER).attributeClass(RECORDING_HANDLER_CLASS)));
    }

    private static final String STANDARD_ENDPOINT_CONFIG = "Standard-Endpoint-Config";

    private static final String RECORDING = "Recording-Endpoint-Config";

    private static final String RECORDING_HANDLERS = "recording-handlers";

    private static final String SOAP_PROTOCOLS = "##SOAP11_HTTP ##SOAP11_HTTP_MTOM ##SOAP12_HTTP ##SOAP12_HTTP_MTOM";

    private static final String RECORDING_HANDLER = "RecordingHandler";

    private static final String RECORDING_HANDLER_CLASS = "org.jboss.ws.common.invocation.RecordingServerHandler";

    private static final String SOAP_HOST = "127.0.0.1";

    private static final String STANDARD_CLIENT_CONFIG = "Standard-Client-Config";
}
