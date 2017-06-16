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
package org.wildfly.swarm.opentracing;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.undertow.WARArchive;

import javax.naming.NamingException;

import static org.junit.Assert.assertTrue;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class OpenTracingWithTraceResolverOnDeploymentTest {

    @Deployment
    public static Archive createDeployment() {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);

        // on real world deployments, these parts would come from a dependency of the target application
        deployment.addClass(MockTracerResolver.class);
        deployment.addPackage(Tracer.class.getPackage());
        deployment.addPackage(MockTracer.class.getPackage());
        deployment.addAsServiceProvider(TracerResolver.class, MockTracerResolver.class);

        return deployment;
    }

    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        return new Swarm().fraction(new OpenTracingFraction());
    }

    @Test
    public void testMockTracerIsAvailable() throws NamingException {
        assertTrue(GlobalTracer.isRegistered());
    }
}
