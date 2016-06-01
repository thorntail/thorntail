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
package org.wildfly.swarm.cli;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Bob McWhirter
 */
public class OptionTest {

    @Test
    public void testLongNoArgs() throws Exception {
        ParseState state = new ParseState("--help");

        AtomicBoolean helpSet = new AtomicBoolean(false);

        Option<Boolean> help = new Option<Boolean>()
                .withLong("help")
                .then((cmd, opt, value) -> {
                    helpSet.set(true);
                });

        assertThat(help.parse(state, null)).isTrue();

        assertThat(helpSet.get()).isTrue();
    }

    @Test
    public void testShortNoArgs() throws Exception {
        ParseState state = new ParseState("-h");

        AtomicBoolean helpSet = new AtomicBoolean(false);

        Option help = new Option<Boolean>()
                .withShort('h')
                .then((cmd, opt, value) -> {
                    helpSet.set(true);
                });

        assertThat(help.parse(state, null)).isTrue();

        assertThat(helpSet.get()).isTrue();
    }

    @Test
    public void testShortWithValueTogether() throws Exception {
        ParseState state = new ParseState("-c=standalone.xml");

        AtomicReference<String> config = new AtomicReference<>(null);

        Option configOpt = new Option<String>()
                .withShort('c')
                .hasValue("<value>")
                .then((cmd, opt, value) -> {
                    config.set(value);
                });

        assertThat(configOpt.parse(state, null)).isTrue();

        assertThat(config.get()).isEqualTo("standalone.xml");
    }

    @Test
    public void testShortWithValueApart() throws Exception {
        ParseState state = new ParseState("-c", "standalone.xml");

        AtomicReference<String> config = new AtomicReference<>(null);

        Option configOpt = new Option<String>()
                .withShort('c')
                .hasValue("<value>")
                .then((cmd, opt, value) -> {
                    config.set(value);
                });

        assertThat(configOpt.parse(state, null)).isTrue();

        assertThat(config.get()).isEqualTo("standalone.xml");
    }

    @Test
    public void testShortWithValueMissing() throws Exception {
        ParseState state = new ParseState("-c");

        AtomicReference<String> config = new AtomicReference<>(null);

        Option<String> configOpt = new Option<String>()
                .withShort('c')
                .hasValue("<value>")
                .then((cmd, opt, value) -> {
                    config.set(value);
                });

        try {
            configOpt.parse(state, null);
            fail("should have throw a missing-argument exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("-c requires an argument");
        }
    }

    @Test
    public void testPropertiesLike() throws Exception {

        ParseState state = new ParseState("-Dfoo", "-Dbar=cheese");

        Properties props = new Properties();

        Option configOpt = new Option<Object>()
                .withShort('D')
                .hasValue("<value>")
                .valueMayBeSeparate(false)
                .then((cmd, opt, value) -> {
                    String[] keyValue = value.split("=");

                    if (keyValue.length == 1) {
                        props.setProperty(keyValue[0], "true");
                    } else {
                        props.setProperty(keyValue[0], keyValue[1]);
                    }
                });

        while (configOpt.parse(state, null)) {
            // do it again
        }

        assertThat(props).hasSize(2);

        System.err.println(props);

        assertThat(props.getProperty("foo")).isEqualTo("true");
        assertThat(props.getProperty("bar")).isEqualTo("cheese");

    }
}
