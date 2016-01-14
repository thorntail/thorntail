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
package org.wildfly.swarm.integration.mail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.integration.base.TestConstants;
import org.wildfly.swarm.undertow.WARArchive;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
//@RunWith(Arquillian.class)
public class SimpleMailTest {

    @Drone
    WebDriver browser;

    @Deployment
    public static Archive createDeployment() {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.staticContent();
        return deployment;
    }

//    @Test
    @RunAsClient
    public void testSimple() throws IOException {
        browser.get(TestConstants.DEFAULT_URL + "static-content.txt");

        assertThat(browser.getPageSource()).isEqualTo("This is static.");
    }

}
