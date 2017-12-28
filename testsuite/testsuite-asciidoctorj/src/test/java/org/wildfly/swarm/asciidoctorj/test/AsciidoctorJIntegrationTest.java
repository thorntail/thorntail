/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.asciidoctorj.test;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
//@RunWith(Arquillian.class)
@DefaultDeployment(main = org.wildfly.swarm.asciidoctorj.test.Main.class)
public class AsciidoctorJIntegrationTest {

    @Drone
    WebDriver browser;

//    @Test
    @RunAsClient
    public void testResource() throws Exception {
        browser.navigate().to("http://localhost:8080/rest/");
        WebElement element = browser.findElement(By.tagName("h2"));
        assertThat(element).isNotNull();
        assertThat(element.getText()).isEqualTo("Hello World");
    }

}
