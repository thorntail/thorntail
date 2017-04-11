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
package org.wildfly.swarm.netflix.ribbon;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Test;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.netflix.ribbon.internal.RibbonArchiveImpl;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class RibbonArchiveTest {

    @Test
    public void testAdvertiseDefaultName() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "myapp.war");
        archive.as(RibbonArchive.class)
                .advertise();

        Asset asset = archive.get(RibbonArchive.REGISTRATION_CONF).getAsset();

        assertThat(asset).isNotNull();
        assertThat(asset).isInstanceOf(StringAsset.class);
        assertThat(((StringAsset) asset).getSource().trim()).isEqualTo("myapp");

        assertThat(archive.as(ServiceActivatorArchive.class).containsServiceActivator(RibbonArchiveImpl.SERVICE_ACTIVATOR_CLASS_NAME)).isTrue();
    }

    @Test
    public void testAdvertiseExplicitName() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "myapp.war");
        archive.as(RibbonArchive.class)
                .advertise("myotherapp");

        Asset asset = archive.get(RibbonArchive.REGISTRATION_CONF).getAsset();

        assertThat(asset).isNotNull();
        assertThat(asset).isInstanceOf(StringAsset.class);
        assertThat(((StringAsset) asset).getSource().trim()).isEqualTo("myotherapp");

        assertThat(archive.as(ServiceActivatorArchive.class).containsServiceActivator(RibbonArchiveImpl.SERVICE_ACTIVATOR_CLASS_NAME)).isTrue();
    }

    @Test
    public void testAdvertiseMultiple() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "myapp.war");
        archive.as(RibbonArchive.class)
                .advertise("service-a")
                .advertise("service-b")
                .advertise("service-c");

        Asset asset = archive.get(RibbonArchive.REGISTRATION_CONF).getAsset();

        assertThat(asset).isNotNull();
        assertThat(asset).isInstanceOf(StringAsset.class);

        String[] services = ((StringAsset) asset).getSource().split("\n");

        assertThat(services).contains("service-a");
        assertThat(services).contains("service-b");
        assertThat(services).contains("service-c");

        assertThat(archive.as(ServiceActivatorArchive.class).containsServiceActivator(RibbonArchiveImpl.SERVICE_ACTIVATOR_CLASS_NAME)).isTrue();
    }

    @Test
    public void testNotAdvertise() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "myapp.war");
        archive.as(RibbonArchive.class);

        assertThat(archive.as(ServiceActivatorArchive.class).containsServiceActivator(RibbonArchiveImpl.SERVICE_ACTIVATOR_CLASS_NAME)).isFalse();
    }

}
