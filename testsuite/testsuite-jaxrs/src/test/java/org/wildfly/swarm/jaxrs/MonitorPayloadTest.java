package org.wildfly.swarm.jaxrs;

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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.monitor.MonitorFraction;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
@Ignore
public class MonitorPayloadTest extends SimpleHttp {

    @Deployment
    public static Archive getDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
        deployment.addClass(SimpleHttp.class);
        deployment.addClass(JaxRsActivator.class);
        deployment.addClass(FailedChecks.class);
        deployment.addAllDependencies();
        deployment.setContextRoot("rest");
        return deployment;
    }

    @CreateSwarm
    public static Swarm getContainer() throws Exception {
        Swarm container = new Swarm();
        container.fraction(new JAXRSFraction());
        container.fraction(new MonitorFraction());
        return container;
    }

    @Test
    @RunAsClient
    public void testHealthIntegration() throws Exception {

        // aggregator / with auth
        Response response = getUrlContents("http://localhost:8080/health");
        System.out.println(response.getBody());
        Assert.assertTrue(
                response.getBody().contains("first") &&
                        response.getBody().contains("second")
        );

        // direct / failure
        response = getUrlContents("http://localhost:8080/rest/v1/failed/first", false);
        Assert.assertEquals("Expected 503", 503, response.getStatus());

        // direct / success
        response = getUrlContents("http://localhost:8080/rest/v1/failed/second", false);
        Assert.assertEquals("Expected 200", 200, response.getStatus());

        // aggregator / failed
        response = getUrlContents("http://localhost:8080/health", true);
        Assert.assertEquals("Expected 503", 503, response.getStatus());
        Assert.assertTrue(response.getBody().contains("first") && response.getBody().contains("UP"));

    }
}
