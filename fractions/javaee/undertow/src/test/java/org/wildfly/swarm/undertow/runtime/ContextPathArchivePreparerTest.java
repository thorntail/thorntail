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

import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.internal.DefaultWarDeploymentFactory;

import static org.fest.assertions.Assertions.assertThat;

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
}
