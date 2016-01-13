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
package org.wildfly.swarm.swagger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Test;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.msc.ServiceActivatorArchive;

import java.util.Scanner;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Lance Ball
 */
public class SwaggerArchiveTest {

    @Test
    public void testSwaggerArchive() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "myapp.war");
        SwaggerArchive swaggerArchive = archive.as(SwaggerArchive.class);

        Asset asset = archive.get(SwaggerArchive.SWAGGER_CONFIGURATION_PATH).getAsset();
        assertThat(asset).isNotNull();
        assertThat(asset).isInstanceOf(SwaggerConfigurationAsset.class);

        assertThat(archive.as(ServiceActivatorArchive.class).containsServiceActivator( SwaggerArchiveImpl.SERVICE_ACTIVATOR_CLASS_NAME )).isTrue();
    }

    @Test
    public void testSwaggerConfiguration() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "myapp.war");

        archive.as(SwaggerArchive.class)
                .setResourcePackages("com.tester.resource", "com.tester.other.resource")
                .setTitle("My Application API")
                .setLicenseUrl("http://myapplication.com/license.txt")
                .setLicense("Use at will")
                .setContextRoot("/tacos")
                .setDescription("This is a description of my API")
                .setHost("api.myapplication.com")
                .setContact("help@myapplication.com")
                .setPrettyPrint(true)
                .setSchemes("http", "https")
                .setTermsOfServiceUrl("http://myapplication.com/tos.txt")
                .setVersion("1.0");



        Asset asset = archive.get(SwaggerArchive.SWAGGER_CONFIGURATION_PATH).getAsset();
        assertThat(asset).isNotNull();
        assertThat(asset).isInstanceOf(SwaggerConfigurationAsset.class);

        SwaggerConfig config = new SwaggerConfig(asset.openStream());
        assertThat(config.get(SwaggerConfig.Key.VERSION)).isEqualTo("1.0");
        assertThat(config.get(SwaggerConfig.Key.TERMS_OF_SERVICE_URL)).isEqualTo("http://myapplication.com/tos.txt");
        assertThat(config.get(SwaggerConfig.Key.PACKAGES)).isEqualTo("com.tester.resource,com.tester.other.resource");
    }
}
