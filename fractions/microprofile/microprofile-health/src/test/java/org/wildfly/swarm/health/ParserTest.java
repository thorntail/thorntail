/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.health;

import java.util.Map;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.State;
import org.junit.Assert;
import org.junit.Test;
import org.wildfly.swarm.microprofile.health.runtime.HealthAnnotationProcessor;
import org.wildfly.swarm.microprofile.health.runtime.HttpContexts;

/**
 * @author Heiko Braun
 */
public class ParserTest {

    @Test
    public void testAttributes() {

        HealthCheckResponse healthStatus = HealthCheckResponse.named("test")
                .up()
                .withData("a", "b")
                .withData("c", "d")
                .build();

        Assert.assertEquals("test", healthStatus.getName());
        Assert.assertSame(State.UP, healthStatus.getState());
        Map<String, Object> data = healthStatus.getData().get();
        Assert.assertEquals(2, data.size());
        Assert.assertEquals("Expected a", "b", data.get("a"));
        Assert.assertEquals("Expected c", "d", data.get("c"));
    }

    @Test
    public void testPathStructures() {
        StringBuilder sb = new StringBuilder();
        HealthAnnotationProcessor.safeAppend(sb, "jboss-web");
        HealthAnnotationProcessor.safeAppend(sb, "/webcontext");
        HealthAnnotationProcessor.safeAppend(sb, "/app");
        Assert.assertEquals("/jboss-web/webcontext/app", sb.toString());

        // --

        sb = new StringBuilder();
        HealthAnnotationProcessor.safeAppend(sb, "jboss-web/");
        HealthAnnotationProcessor.safeAppend(sb, "/webcontext");
        HealthAnnotationProcessor.safeAppend(sb, "app");
        Assert.assertEquals("/jboss-web/webcontext/app", sb.toString());

        // --

        sb = new StringBuilder();
        HealthAnnotationProcessor.safeAppend(sb, "jboss-web");
        HealthAnnotationProcessor.safeAppend(sb, "webcontext");
        HealthAnnotationProcessor.safeAppend(sb, "app");
        Assert.assertEquals("/jboss-web/webcontext/app", sb.toString());

        // --

        sb = new StringBuilder();
        HealthAnnotationProcessor.safeAppend(sb, "jboss-web/");
        HealthAnnotationProcessor.safeAppend(sb, "webcontext/");
        HealthAnnotationProcessor.safeAppend(sb, "app/");
        Assert.assertEquals("/jboss-web/webcontext/app", sb.toString());

        // --

        sb = new StringBuilder();
        HealthAnnotationProcessor.safeAppend(sb, "/jboss-web/");
        HealthAnnotationProcessor.safeAppend(sb, "/webcontext/");
        HealthAnnotationProcessor.safeAppend(sb, "/app/");
        Assert.assertEquals("/jboss-web/webcontext/app", sb.toString());


    }

    @Test
    public void testJsonEncoding() {
        HealthCheckResponse healthStatus = HealthCheckResponse
                .named("test")
                .withData("a", "b")
                .withData("c", "d")
                .up()
                .build();

        String s = HttpContexts.toJson(healthStatus);
        Assert.assertEquals("{\"name\":\"test\",\"state\":\"UP\",\"data\": {\"a\":\"b\",\"c\":\"d\"}}", s);

    }
}
