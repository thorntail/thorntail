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
package org.wildfly.swarm.jaxrs;

import java.util.Properties;

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
import org.wildfly.swarm.management.ManagementFraction;
import org.wildfly.swarm.monitor.MonitorFraction;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
@Ignore
public class MonitorSecurityTest extends SimpleHttp {

    @Deployment
    public static Archive getDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
        deployment.addClass(TimeResource.class);
        deployment.addClass(SimpleHttp.class);
        deployment.addClass(JaxRsActivator.class);
        deployment.addClass(SuccessfulChecks.class);
        deployment.addAllDependencies();
        deployment.setContextRoot("rest");
        return deployment;
    }

    @CreateSwarm
    public static Swarm getContainer() throws Exception {
        Swarm container = new Swarm();
        container.fraction(new JAXRSFraction());
        container.fraction(new MonitorFraction().securityRealm("TestRealm"));
        container.fraction(
                new ManagementFraction()
                        .securityRealm("TestRealm", (realm) -> {
                            realm.inMemoryAuthentication((authn) -> {
                                authn.add(new Properties() {{
                                    put("admin", "password");
                                }}, true);
                            });
                            realm.inMemoryAuthorization();
                        })
        );

        return container;
    }

    @Test
    @RunAsClient
    public void testHealthIntegration() throws Exception {

        Response response = null;
        // aggregator / with auth

        response = getUrlContents("http://localhost:8080/health");
        Assert.assertTrue(
                response.getBody().contains("first") &&
                        response.getBody().contains("second")
        );

        // aggregator / no auth
        response = getUrlContents("http://localhost:8080/health", false);
        Assert.assertEquals("Expected 401 when accessing aggregator wihthout credentials", 401, response.getStatus());

        // direct / no auth
        response = getUrlContents("http://localhost:8080/rest/v1/success/first", false);
        Assert.assertEquals("Expected 401 when directly accessing secured health endpoint wihthout credentials", 401, response.getStatus());

        // direct // with auth
        response = getUrlContents("http://localhost:8080/rest/v1/success/first", true);
        Assert.assertEquals("Expected 200 when directly accessing secured health endpoint with credentials", 200, response.getStatus());
        Assert.assertTrue(response.getBody().contains("first") && response.getBody().contains("UP"));

        // verify other resources remain untouched
        response = getUrlContents("http://localhost:8080/rest/v1/another-app/time");
        Assert.assertTrue(response.getBody().contains("Time"));
    }
}
