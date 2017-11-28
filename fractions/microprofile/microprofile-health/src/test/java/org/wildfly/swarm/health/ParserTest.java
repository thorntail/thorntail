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

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.swarm.microprofile.health.runtime.HealthAnnotationProcessor;
import org.wildfly.swarm.microprofile.health.runtime.HttpContexts;

/**
 * @author Heiko Braun
 */
public class ParserTest {

    // see https://issues.jboss.org/browse/SWARM-505
    @Test
    public void testAttributes() {

        HealthStatus healthStatus = HealthStatus.named("test")
                .up()
                .withAttribute("a", "b")
                .withAttribute("c", "d");

        String message = healthStatus.getMessage().get();
        Assert.assertTrue("Expected a", message.contains("a"));
        Assert.assertTrue("Expected c", message.contains("c"));
        System.out.println(message);
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
        org.eclipse.microprofile.health.HealthCheckResponse healthStatus = org.eclipse.microprofile.health.HealthCheckResponse
                .named("test")
                .withData("a", "b")
                .withData("c", "d")
                .up()
                .build();

        String s = HttpContexts.toJson(healthStatus);
        System.out.println(s);
    }
}
