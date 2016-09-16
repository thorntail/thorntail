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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class JAXRSJarArquillianTest {

    @Deployment(testable = false)
    public static Archive createDeployment() throws Exception {
        JARArchive deployment = ShrinkWrap.create(JARArchive.class, "myapp.jar");
        deployment.addClass(HealthCheckResource.class);
        deployment.addClass(CustomJsonProvider.class);
        deployment.addClass(MyResource.class);
        return deployment;
    }

    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        return new Swarm().fraction(new JAXRSFraction());
    }

    @Test
    @RunAsClient
    public void testResource() throws InterruptedException {
        browser.navigate().to("http://localhost:8080/health/monitor/health-secure");
        assertThat(browser.getPageSource()).contains("UP");
    }

    @RunAsClient
    @Test
    public void testSimple() throws IOException, InterruptedException {
        browser.navigate().to("http://localhost:8080/");
        assertThat(browser.getPageSource()).contains("Howdy at ");
    }

    @Drone
    WebDriver browser;
}
