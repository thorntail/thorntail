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
package org.wildfly.swarm.undertow;

import org.junit.Test;
import org.wildfly.swarm.undertow.descriptors.JBossWebAsset;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class JbossWebAssetTest {

    @Test
    public void testEmpty() throws Exception {
        JBossWebAsset asset = new JBossWebAsset();

        assertThat(asset.isRootSet()).isFalse();
        assertThat(asset.getContextRoot()).isNull();

        asset.setContextRoot("/myRoot");
        assertThat(asset.isRootSet()).isTrue();
        assertThat(asset.getContextRoot()).isEqualTo("/myRoot");

        asset.setContextRoot("/anotherRoot");
        assertThat(asset.isRootSet()).isTrue();
        assertThat(asset.getContextRoot()).isEqualTo("/anotherRoot");

    }
}
