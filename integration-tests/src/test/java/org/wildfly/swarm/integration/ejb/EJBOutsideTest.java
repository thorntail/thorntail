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
package org.wildfly.swarm.integration.ejb;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.arquillian.adapter.ArtifactDependencies;
import org.wildfly.swarm.integration.base.TestConstants;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import javax.ejb.EJB;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
public class EJBOutsideTest {

    @Drone
    WebDriver browser;

    private GreeterEJB greeter;

    @Deployment
    public static Archive createDeployment() throws Exception {
        return ShrinkWrap.create(JAXRSArchive.class, "ejb-test.war")
                .addResource(MyResource.class)
                .addClass(GreeterEJB.class);
    }

    @ArtifactDependencies
    public static List<String> appDependencies() {
        return Arrays.asList(
                "org.wildfly.swarm:ejb",
                "org.wildfly.swarm:jaxrs"
        );
    }

    @Test
    @RunAsClient
    public void testFromOutside() throws IOException {
        browser.navigate().to(TestConstants.DEFAULT_URL);

        assertThat(browser.getPageSource()).contains("Howdy from EJB");
    }
}
