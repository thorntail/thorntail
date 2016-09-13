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
package org.wildfly.swarm.arquillian.adapter;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class MavenProfileLoaderTest {

    @Before
    public void reset() throws Exception {
        final Field flagField = MavenProfileLoader.class.getDeclaredField("profilesDiscovered");
        flagField.setAccessible(true);
        flagField.setBoolean(null, false);

        final Field arrayField = MavenProfileLoader.class.getDeclaredField("profiles");
        arrayField.setAccessible(true);
        arrayField.set(null, new String[0]);
    }

    @Test
    public void testNullCommand() throws Exception {
        System.clearProperty("env.MAVEN_CMD_LINE_ARGS");

        String[] profiles = MavenProfileLoader.determineProfiles();

        assertThat(profiles).hasSize(0);
    }

    @Test
    public void testEmptyCommand() throws Exception {
        System.setProperty("env.MAVEN_CMD_LINE_ARGS", "");

        String[] profiles = MavenProfileLoader.determineProfiles();

        assertThat(profiles).hasSize(0);
    }

    @Test
    public void testNoProfile() throws Exception {
        System.setProperty("env.MAVEN_CMD_LINE_ARGS", "mvn clean install");

        String[] profiles = MavenProfileLoader.determineProfiles();

        assertThat(profiles).hasSize(0);
    }

    @Test
    public void testOneProfile() throws Exception {
        System.setProperty("env.MAVEN_CMD_LINE_ARGS", "mvn clean install -Pwildfly");

        String[] profiles = MavenProfileLoader.determineProfiles();

        assertThat(profiles).hasSize(1);
        assertThat(profiles[0]).contains("wildfly");
    }

    @Test
    public void testTwoProfiles() throws Exception {
        System.setProperty("env.MAVEN_CMD_LINE_ARGS", "mvn clean install -Pwildfly,testing");

        String[] profiles = MavenProfileLoader.determineProfiles();

        assertThat(profiles).hasSize(2);
        assertThat(profiles[0]).contains("wildfly");
        assertThat(profiles[1]).contains("testing");
    }

    @Test
    public void testThreeProfiles() throws Exception {
        System.setProperty("env.MAVEN_CMD_LINE_ARGS", "mvn clean install -Pwildfly,testing,another");

        String[] profiles = MavenProfileLoader.determineProfiles();

        assertThat(profiles).hasSize(3);
        assertThat(profiles[0]).contains("wildfly");
        assertThat(profiles[1]).contains("testing");
        assertThat(profiles[2]).contains("another");
    }
}
