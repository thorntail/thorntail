/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.cdi.jaxws.test;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@DefaultDeployment
public class CdiJaxwsTest {
    @Test
    public void cdiJaxws() throws MalformedURLException {
        QName serviceName = new QName("http://wildfly-swarm.io/Greeter", "GreeterService");
        Service service = Service.create(new URL("http://localhost:8080/GreeterService?wsdl"), serviceName);
        GreeterService greeter = service.getPort(GreeterService.class);

        assertEquals("Hello! WebServiceContext is present", greeter.hello());
    }
}
