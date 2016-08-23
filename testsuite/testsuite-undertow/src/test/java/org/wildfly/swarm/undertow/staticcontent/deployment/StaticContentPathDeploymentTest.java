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
package org.wildfly.swarm.undertow.staticcontent.deployment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.staticcontent.StaticContentCommonTests;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class StaticContentPathDeploymentTest implements StaticContentCommonTests {

    @Deployment(testable = false)
    public static Archive getThird() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.staticContent("foo");
        return deployment;
    }

    @Test
    public void testStaticContentWithBase() throws Exception {
        assertContains("", "This is foo/index.html.");
        assertContains("/index.html", "This is foo/index.html.");
    }

    public void assertContains(String path, String text) throws Exception {
        assertThat(fetch("http://localhost:8080" + path)).contains(text);
    }

    public void assertNotFound(String path) throws Exception {
        try {
            fetch("http://localhost:8080/" + path);
            fail("FileNotFoundException expected but content found for path " + path);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(FileNotFoundException.class);
        }
    }

    protected String fetch(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        StringBuffer buffer = new StringBuffer();
        try (InputStream in = url.openStream()) {
            int numRead = 0;
            while (numRead >= 0) {
                byte[] b = new byte[1024];
                numRead = in.read(b);
                if (numRead < 0) {
                    break;
                }
                buffer.append(new String(b, 0, numRead));
            }
        }

        return buffer.toString();
    }

    private void assertFileChangesReflected(String context) throws Exception {
        if (context.length() > 0 || !context.endsWith("/")) {
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
}
