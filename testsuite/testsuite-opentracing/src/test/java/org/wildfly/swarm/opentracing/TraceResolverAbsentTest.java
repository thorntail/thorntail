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

import io.opentracing.util.GlobalTracer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;

import static org.junit.Assert.assertFalse;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class TraceResolverAbsentTest {

    @Deployment
    public static Archive createDeployment() {
        return ShrinkWrap.create(WARArchive.class);
    }

    @Test
    public void testMockTracerIsNotAvailable() {
        assertFalse(GlobalTracer.isRegistered());
    }
}
