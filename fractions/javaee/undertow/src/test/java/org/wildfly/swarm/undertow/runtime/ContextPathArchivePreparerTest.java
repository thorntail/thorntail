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
package org.wildfly.swarm.undertow.runtime;

import static org.fest.assertions.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.jboss.shrinkwrap.api.Node;
import org.junit.Test;
import org.wildfly.swarm.container.config.ConfigViewFactory;
import org.wildfly.swarm.container.config.ConfigViewImpl;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.internal.DefaultWarDeploymentFactory;
import org.wildfly.swarm.undertow.internal.UndertowExternalMountsAsset;

/**
 * @author Ken Finnigan
 */
public class ContextPathArchivePreparerTest {

    @Test
    public void testDefaultContextRoot() throws Exception {
        WARArchive archive = DefaultWarDeploymentFactory.archiveFromCurrentApp();

        assertThat(archive.getContextRoot()).isNull();

        new ContextPathArchivePreparer().prepareArchive(archive);

        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("/");
    }

    @Test
    public void testDefaultContextRootWontOverride() throws Exception {
        WARArchive archive = DefaultWarDeploymentFactory.archiveFromCurrentApp();

        assertThat(archive.getContextRoot()).isNull();

        archive.setContextRoot("myRoot");
        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("myRoot");

        new ContextPathArchivePreparer().prepareArchive(archive);

        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("myRoot");
    }

    @Test
    public void testContextPathProperty() throws Exception {
        WARArchive archive = DefaultWarDeploymentFactory.archiveFromCurrentApp();

        assertThat(archive.getContextRoot()).isNull();

        ContextPathArchivePreparer preparer = new ContextPathArchivePreparer();
        preparer.contextPath.set("/another-root");
        preparer.prepareArchive(archive);

        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("/another-root");
    }
    
    @Test
    public void testExternalMount() throws Exception {
        WARArchive archive = DefaultWarDeploymentFactory.archiveFromCurrentApp();

        assertThat(archive.getContextRoot()).isNull();
        
        URL url = getClass().getClassLoader().getResource("mounts.yml");
        ConfigViewFactory factory = new ConfigViewFactory(new Properties());
        factory.load("test", url);
        ConfigViewImpl view = factory.get();
        view.withProfile("test");

        List<String> mounts = view.resolve("swarm.context.mounts").as(List.class).getValue();

        ContextPathArchivePreparer preparer = new ContextPathArchivePreparer();     
        preparer.mounts = mounts;
        
        preparer.prepareArchive(archive);

        Node externalMount = archive.get(WARArchive.EXTERNAL_MOUNT_PATH);
        assertThat(externalMount).isNotNull();
        assertThat(externalMount.getAsset()).isInstanceOf(UndertowExternalMountsAsset.class);
        UndertowExternalMountsAsset externalMountAsset = (UndertowExternalMountsAsset) externalMount.getAsset();
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(externalMountAsset.openStream())); ){
            assertThat(reader.readLine()).endsWith("external1");
            assertThat(reader.readLine()).endsWith("external2");
        }        
        
    }
}
