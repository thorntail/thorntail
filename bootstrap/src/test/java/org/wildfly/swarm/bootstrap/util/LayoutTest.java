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
package org.wildfly.swarm.bootstrap.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class LayoutTest {

    @Test
    public void testSingletoness() throws Exception {
        assertThat(Layout.getInstance()).isSameAs(Layout.getInstance());
    }

    @Test
    public void testNotUberJar() throws Exception {
        Layout layout = Layout.getInstance();
        assertThat(layout.isUberJar()).isFalse();
    }

    @Test
    public void testBootstrapClassLoader() throws Exception {
        Layout layout = Layout.getInstance();
        assertThat(layout.getBootstrapClassLoader()).isSameAs(Layout.class.getClassLoader());
    }

    @Test
    public void testGetManifest() throws Exception {
        Layout layout = Layout.getInstance();

        System.err.println(layout.getManifest());

    }
}
