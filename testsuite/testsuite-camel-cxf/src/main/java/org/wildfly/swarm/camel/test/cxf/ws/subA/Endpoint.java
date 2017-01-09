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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace = "http://wildfly.camel.test.cxf")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface Endpoint {

    @WebMethod(operationName = "echoString", action = "urn:EchoString")
    String echo(@WebParam(name = "input", header = true, targetNamespace = "http://wildfly.camel.test.cxf") String input);
}
