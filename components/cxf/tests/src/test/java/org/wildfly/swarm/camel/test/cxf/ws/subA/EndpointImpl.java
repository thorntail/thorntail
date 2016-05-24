/*
 * #%L
 * Camel CXF :: Tests
 * %%
 * Copyright (C) 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.wildfly.swarm.camel.test.cxf.ws.subA;

import javax.jws.WebService;

import org.jboss.logging.Logger;

/**
 * A simple web service endpoint
 *
 * @author thomas.diesler@jboss.com
 * @since 28-Aug-2012
 */
@WebService(serviceName="EndpointService", portName="EndpointPort", targetNamespace="http://wildfly.camel.test.cxf", endpointInterface = "org.wildfly.swarm.camel.test.cxf.ws.subA.Endpoint")
public class EndpointImpl {

    private static Logger log = Logger.getLogger(EndpointImpl.class);

    public String echo(String input) {
        log.info("echo: " + input);
        return "Hello " + input;
    }
}
