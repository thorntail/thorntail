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
package org.wildfly.swarm.topology;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.junit.Assert.assertNotNull;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class TopologyArquillianTest {

    @Deployment
    public static Archive createDeployment() {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addClass(MyApplication.class);
        deployment.addClass(MyResource.class);
        return deployment;
    }

    @ArquillianResource
    private ServiceRegistry registry;

    @Test
    public void testNothing() {
        ServiceController<?> manager = this.registry.getService(ServiceName.of("swarm", "topology"));
        assertNotNull( manager );

        ServiceController<?> httpAdvert = this.registry.getService(ServiceName.of("swarm", "topology", "register", "tacos", "http"));
        assertNotNull( httpAdvert );

        ServiceController<?> httpsAdvert = this.registry.getService(ServiceName.of("swarm", "topology", "register", "tacos", "https"));
        assertNotNull( httpsAdvert );
    }

}
