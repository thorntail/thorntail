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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class TraceResolverOnDeploymentTest {

    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);

        // on real world deployments, these parts would come from a dependency of the target application
        deployment.addClass(MockTracerResolver.class);
        deployment.addPackage(MockTracer.class.getPackage());
        deployment.addAsServiceProvider(TracerResolver.class, MockTracerResolver.class);

        // this is a simple servlet, that we can hit with our tests
        deployment.addClass(SimpleServlet.class);
        deployment.addClass(AsyncServlet.class);

        return deployment;
    }

    @After
    public void after() {
        MockTracerResolver.TRACER_INSTANCE.reset();
    }

    @Test
    public void testMockTracerIsAvailable() {
        assertTrue(GlobalTracer.isRegistered());
    }

    @Test
    public void testOneSpanIsReportedPerRequest() throws IOException {
        MockTracer tracer = MockTracerResolver.TRACER_INSTANCE;
        assertEquals(0, tracer.finishedSpans().size());
        hitEndpoint("http://localhost:8080/_opentracing/hello");
        assertEquals(2, tracer.finishedSpans().size());
    }

    @Test
    public void testAsyncEndpoint() throws IOException {
        MockTracer tracer = MockTracerResolver.TRACER_INSTANCE;
        assertEquals(0, tracer.finishedSpans().size());
        hitEndpoint("http://localhost:8080/_opentracing/async");
        assertEquals(1, tracer.finishedSpans().size());
    }

    private void hitEndpoint(String url) throws IOException {
        InputStream response = new URL(url).openStream();
        String contents;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(response))) {
            contents = buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
