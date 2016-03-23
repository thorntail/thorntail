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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.management.ManagementFraction;
import org.wildfly.swarm.monitor.MonitorFraction;

/**
 * @author Bob McWhirter
 */
public class JAXRSInVmTest extends SimpleHttp {

    @Test
    public void testHealthIntegration() throws Exception {

        Container container = new Container();
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
        container.start();

        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
        deployment.addClass(TimeResource.class);
        deployment.addClass(HealthCheckResource.class);

        container.deploy(deployment);

        // verify listing of subresources
        SimpleHttp.Response endpointList = getUrlContents("http://localhost:8080/health");
        Assert.assertTrue(endpointList.getBody().contains("links")); //hateos structure

        // verify direct access to secure resources
        SimpleHttp.Response response = getUrlContents("http://localhost:8080/app/health-secure"); // 403
        Assert.assertTrue("Expected 403 when directly accessing secured health endpoint", response.getStatus() == 403);

        // verify indirect access to secure resources
        response = getUrlContents("http://localhost:8080/health/app/health-secure");
        Assert.assertTrue(response.getBody().contains("UP"));

        // verify indirect access, without auth, to secure resources
        response = getUrlContents("http://localhost:8080/health/app/health-secure", false);
        Assert.assertEquals(401, response.getStatus());

        // verify direct access to insecure resources
        response = getUrlContents("http://localhost:8080/app/health-insecure");
        Assert.assertTrue(response.getBody().contains("UP"));

        // verify indirect access, without auth, to insecure resources
        response = getUrlContents("http://localhost:8080/health/app/health-insecure", false);
        Assert.assertEquals(200, response.getStatus());

        // verify indirect access to insecure resources
        response = getUrlContents("http://localhost:8080/health/app/health-insecure");
        Assert.assertTrue(response.getBody().contains("UP"));

        // verify other resources remain untouched
        response = getUrlContents("http://localhost:8080/another-app/time");
        Assert.assertTrue(response.getBody().contains("Time"));

        container.stop();

    }
}
