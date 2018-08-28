/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import org.junit.Test;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.spi.api.config.ConfigView;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.wildfly.swarm.cli.CommandLine.HELP;
import static org.wildfly.swarm.cli.CommandLine.PROPERTIES_URL;
import static org.wildfly.swarm.cli.CommandLine.PROPERTY;
import static org.wildfly.swarm.cli.CommandLine.VERSION;

/**
 * @author Bob McWhirter
 */
public class CommandLineTest {

    @Test
    public void testEmpty() throws Exception {
        CommandLine cmd = CommandLine.parse();

        assertThat(cmd.get(HELP)).isFalse();
        assertThat(cmd.get(VERSION)).isFalse();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testHelpLong() throws Exception {
        CommandLine cmd = CommandLine.parse("--help");

        assertThat(cmd.get(HELP)).isTrue();
        assertThat(cmd.get(VERSION)).isFalse();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testHelpShort() throws Exception {
        CommandLine cmd = CommandLine.parse("-h");

        assertThat(cmd.get(HELP)).isTrue();
        assertThat(cmd.get(VERSION)).isFalse();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testVersionLong() throws Exception {
        CommandLine cmd = CommandLine.parse("--version");

        assertThat(cmd.get(HELP)).isFalse();
        assertThat(cmd.get(VERSION)).isTrue();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testVersionShort() throws Exception {
        CommandLine cmd = CommandLine.parse("-v");

        assertThat(cmd.get(HELP)).isFalse();
        assertThat(cmd.get(VERSION)).isTrue();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testProperties() throws Exception {
        CommandLine cmd = CommandLine.parse("-Dfoo", "-Dbar=cheese");

        assertThat(cmd.get(HELP)).isFalse();
        assertThat(cmd.get(VERSION)).isFalse();
        assertThat(cmd.extraArguments()).isEmpty();

        assertThat(cmd.get(PROPERTY)).hasSize(2);
        assertThat(cmd.get(PROPERTY).get("foo")).isEqualTo("true");
        assertThat(cmd.get(PROPERTY).get("bar")).isEqualTo("cheese");
    }

    @Test
    public void testLongArgWithEqual() throws Exception {
        String fileName = "my.properties";
        String expectedPath = new File(System.getProperty("user.dir"), fileName).toURI().toURL().toString();

        CommandLine cmd = CommandLine.parse("--properties=" + fileName);

        assertThat(cmd.get(PROPERTIES_URL).toString()).isEqualTo(expectedPath);
    }

    @Test
    public void testLongArgWithoutEqual() throws Exception {
        String fileName = "my.properties";
        String expectedPath = new File(System.getProperty("user.dir"), fileName).toURI().toURL().toString();

        CommandLine cmd = CommandLine.parse("--properties", fileName);

        assertThat(cmd.get(PROPERTIES_URL).toString()).isEqualTo(expectedPath);
    }

    @Test
    public void testComplex() throws Exception {
        CommandLine cmd = CommandLine.parse(
                "--help",
                "--version",
                "-Dfoo",
                "ken",
                "-Dbar=cheese",
                "heiko",
                "bob");

        assertThat(cmd.get(HELP)).isTrue();
        assertThat(cmd.get(VERSION)).isTrue();
        assertThat(cmd.extraArguments()).hasSize(3);
        assertThat(cmd.extraArguments().get(0)).isEqualTo("ken");
        assertThat(cmd.extraArguments().get(1)).isEqualTo("heiko");
        assertThat(cmd.extraArguments().get(2)).isEqualTo("bob");

        assertThat(cmd.get(PROPERTY)).hasSize(2);
        assertThat(cmd.get(PROPERTY).get("foo")).isEqualTo("true");
        assertThat(cmd.get(PROPERTY).get("bar")).isEqualTo("cheese");


    }

    @Test
    public void testApplyPropertiesWithPropertiesHierarchy() throws Exception {
        // Given
        URL props = getClass().getClassLoader().getResource("hierarchy.properties");
        CommandLine cmd = CommandLine.parse("--properties", props.toExternalForm());
        Swarm swarm = mock(Swarm.class);

        // When
        cmd.applyProperties(swarm);

        // Then
        verify(swarm).withProperty("parent.children", "child1,child2");
        verify(swarm).withProperty("parent.children.child1.name", "Jack");
        verify(swarm).withProperty("parent.children.child2.name", "Jill");
        verifyNoMoreInteractions(swarm);
    }

}
