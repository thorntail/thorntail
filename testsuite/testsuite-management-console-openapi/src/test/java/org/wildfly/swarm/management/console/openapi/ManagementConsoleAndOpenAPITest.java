package org.wildfly.swarm.management.console.openapi;

/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
import static org.fest.assertions.Assertions.assertThat;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.microprofile.openapi.Activator;
import org.wildfly.swarm.microprofile.openapi.TheUltimateResource;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @see THORN-2047
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class ManagementConsoleAndOpenAPITest {

    @Drone
    WebDriver browser;

    @Deployment
    public static Archive createDeployment() throws Exception {
        return ShrinkWrap.create(WARArchive.class).addClasses(TheUltimateResource.class, Activator.class);
    }

    @Test
    @RunAsClient
    public void testBothFractionsWorkTogether() throws Exception {
        // test mgmt console
        browser.navigate().to("http://localhost:8080/console");
        assertThat(browser.getTitle()).isEqualToIgnoringCase("Management Interface");

        //test OpenAPI
        String content = getUrlContent("http://localhost:8080/openapi");
        Assert.assertNotNull(content);
        String theAnswer = getUrlContent("http://localhost:8080/api/answertolifeuniverseandall/tellme");
        Assert.assertEquals("42", theAnswer);
    }

    private static String getUrlContent(String url) throws Exception {
        return IOUtils.toString(new URI(url), "UTF-8");
    }

}
