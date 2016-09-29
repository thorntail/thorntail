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
package org.wildfly.swarm.messaging;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.junit.Assert.assertNotNull;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class SimplifiedRemoteMessagingArquillianTest {

    @Deployment
    public static Archive createDeployment() {
        JARArchive deployment = ShrinkWrap.create(JARArchive.class);
        deployment.add(EmptyAsset.INSTANCE, "nothing");
        deployment.addModule( "org.wildfly.swarm.messaging" );
        return deployment;
    }

    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        return new Swarm()
                .fraction(new MessagingFraction()
                        .server("default", server -> {
                            server.remoteConnection( "remote-activemq", (config)->{
                                // using default host/port
                                config.jndiName( "java:/jms/remoteCF" );
                            });
                        })
                );
    }

    @ArquillianResource
    InitialContext context;

    @Test
    public void testDefaultConnectionFactory() throws Exception {
        ConnectionFactory factory = (ConnectionFactory) context.lookup("java:/jms/remoteCF");
        assertNotNull(factory);
    }

}
