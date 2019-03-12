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
package org.wildfly.swarm.swarmtool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.env.WildFlySwarmManifest;

import static org.fest.assertions.Assertions.assertThat;

public class MainTest {

    static Result runTool(String... args) throws Exception {
        final OutputStream err = new ByteArrayOutputStream();
        final OutputStream out = new ByteArrayOutputStream();
        final PrintStream origErr = System.err;
        final PrintStream origOut = System.out;
        final Result result = new Result();

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));
            final String[] fullArgs = Arrays.copyOf(args, args.length + 1);
            fullArgs[fullArgs.length - 1] = "--output-dir=target/test-output";

            final long t = System.currentTimeMillis();
            result.jarFile(Main.generateSwarmJar(fullArgs));
            //origErr.println("generateSwarmJar took " + (System.currentTimeMillis() - t) + "ms");

        } catch (Main.ExitException e) {
            result.exitStatus = e.status;
            result.exitMessage = e.getMessage();
        } finally {
            System.setErr(origErr);
            System.setOut(origOut);
        }

        result.err = err.toString();
        result.out = out.toString();

        //System.err.println(result.err);
        //System.out.println(result.err);

        return result;
    }

    static String getResourcePath(String name) throws URISyntaxException {
        return Paths.get(MainTest.class.getClassLoader()
                .getResource(name)
                .toURI())
                .toString();
    }

    Properties swarmProperties(Result result) throws IOException {
        try (InputStream in = result.archive
                .get(WildFlySwarmManifest.CLASSPATH_LOCATION)
                .getAsset()
                .openStream()) {
            WildFlySwarmManifest manifest = new WildFlySwarmManifest(in);
            return manifest.getProperties();
        }
    }

    static synchronized Result getBigJar() throws Exception {
        if (bigJar == null) {
            bigJar = runTool(getResourcePath("simple-servlet.war"),
                             "--name=big",
                             "-Dfoo=bar",
                             "-Dham=biscuit",
                             "--property-file=" + getResourcePath("test.properties"),
                             "--fractions=blarg,a:b:c:d",
                             "--repos=https://repository-projectodd.forge.cloudbees.com/snapshot");
            assertThat(bigJar.exitStatus).isEqualTo(0);
        }
        return bigJar;
    }

    static synchronized Result getLittleJar() throws Exception {
        if (littleJar == null) {
            littleJar = runTool(getResourcePath("simple-servlet.war"),
                                "--name=little",
                                "--no-bundle-deps",
                                "--main=org.foo.bar.Main",
                                "--modules", getResourcePath("modules"),
                                "--repos=https://repository-projectodd.forge.cloudbees.com/snapshot");
            assertThat(littleJar.exitStatus).isEqualTo(0);
        }
        return littleJar;
    }

    @Test
    public void properties() throws Exception {
        final Properties big = swarmProperties(getBigJar());
        assertThat(big.get("cheese")).isEqualTo("biscuit");
        assertThat(big.get("foo")).isEqualTo("bar");
        // -D overrides properties file
        assertThat(big.get("ham")).isEqualTo("biscuit");

        final Properties little = swarmProperties(getLittleJar());
        assertThat(little.get("cheese")).isNull();
        assertThat(little.get("foo")).isNull();
        assertThat(little.get("ham")).isNull();
    }

    @Test
    public void dependencies() throws Exception {
        assertThat(getBigJar().archive.contains("/m2repo")).isTrue();
        assertThat(getLittleJar().archive.contains("/m2repo")).isFalse();
    }

    @Test
    public void addingModules() throws Exception {
        assertThat(getLittleJar().archive.contains("/modules/test/module/main/module.xml")).isTrue();
        assertThat(getBigJar().archive.contains("/modules/test/module/main/module.xml")).isFalse();
    }

    @Test
    public void invalidFractions() throws Exception {
        final Result result = getBigJar();

        assertThat(result.err).contains("Invalid fraction specifier: a:b:c:d");
        assertThat(result.err).contains("Unknown fraction: blarg");

    }

    static class Result {
        public int exitStatus = 0;

        public String exitMessage = null;

        public File jarFile = null;

        public WebArchive archive = null;

        public String err = null;

        public String out = null;

        public void jarFile(File f) {
            this.jarFile = f;
            this.archive = ShrinkWrap.createFromZipFile(WebArchive.class, f);
        }
    }

    static Result bigJar = null;
    static Result littleJar = null;
}
