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
package org.wildfly.swarm.integration.staticcontent.deployment;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.integration.staticcontent.StaticContentCommonTests;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Bob McWhirter
 */
public class StaticContentDeploymentTest extends AbstractWildFlySwarmTestCase implements StaticContentCommonTests {

    @Test
    public void testStaticContent() throws Exception {
        Container container = newContainer();
        container.start();
        try {
            WARArchive deployment = ShrinkWrap.create(WARArchive.class);
            deployment.staticContent();
            container.deploy(deployment);
            assertBasicStaticContentWorks("");
            assertFileChangesReflected("");
        } finally {
            container.stop();
        }
    }

    @Test
    public void testStaticContentWithContext() throws Exception {
        Container container = newContainer();
        container.start();
        try {
            WARArchive deployment = ShrinkWrap.create(WARArchive.class);
            deployment.setContextRoot("/static");
            deployment.staticContent();
            container.deploy(deployment);
            assertBasicStaticContentWorks("static");
            assertFileChangesReflected("static");
        } finally {
            container.stop();
        }
    }

    @Test
    public void testStaticContentWithBase() throws Exception {
        Container container = newContainer();
        container.start();
        try {
            WARArchive deployment = ShrinkWrap.create(WARArchive.class);
            deployment.staticContent("foo");
            container.deploy(deployment);
            assertContains("", "This is foo/index.html.");
            assertContains("index.html", "This is foo/index.html.");
        } finally {
            container.stop();
        }
    }

    private void assertFileChangesReflected(String context) throws Exception {
        if (context.length() > 0 && !context.endsWith("/")) {
            context = context + "/";
        }
        Path tmpDir = Paths.get(System.getProperty("user.dir"), "src", "main", "webapp", "tmp");
        Files.createDirectories(tmpDir);
        Path newFile = tmpDir.resolve("new-file.txt");
        try {
            Files.write(newFile, "This is new-file.txt.".getBytes());
            assertContains(context + "tmp/new-file.txt", "This is new-file.txt.");
            Files.write(newFile, "This is updated new-file.txt.".getBytes());
            assertContains(context + "tmp/new-file.txt", "This is updated new-file.txt.");
        } finally {
            Files.deleteIfExists(newFile);
        }
    }

    public void assertContains(String path, String text) throws Exception {
        assertThat(fetch(DEFAULT_URL + path)).contains(text);
    }

    public void assertNotFound(String path) throws Exception {
        try {
            fetch(DEFAULT_URL + path);
            fail("FileNotFoundException expected but content found for path " + path);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(FileNotFoundException.class);
        }
    }
}
