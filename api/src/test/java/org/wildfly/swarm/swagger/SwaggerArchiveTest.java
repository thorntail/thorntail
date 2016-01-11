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

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Lance Ball
 */
public class SwaggerArchiveTest {

    @Test
    public void testRegister() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "myapp.war");
        archive.as(SwaggerArchive.class).register("com.tester.resource");

        Asset asset = archive.get(SwaggerArchive.SWAGGER_CONFIGURATION_PATH).getAsset();

        assertThat(asset).isNotNull();
        assertThat(asset).isInstanceOf(StringAsset.class);
        assertThat(((StringAsset) asset).getSource().trim()).isEqualTo("com.tester.resource");

        assertThat( archive.as(ServiceActivatorArchive.class).containsServiceActivator( SwaggerArchiveImpl.SERVICE_ACTIVATOR_CLASS_NAME )).isTrue();
    }
}
