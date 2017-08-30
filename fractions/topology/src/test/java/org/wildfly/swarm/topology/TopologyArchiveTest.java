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
package org.wildfly.swarm.topology;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.spi.api.JARArchive;
import static org.fest.assertions.Assertions.*;

/**
 * @author Bob McWhirter
 */
public class TopologyArchiveTest {

    @Test
    public void testMultipleCastingToTopologyArchive() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.as(TopologyArchive.class).advertise("foo");

        assertThat( archive.as( TopologyArchive.class).advertisements() ).hasSize(1);
        assertThat( archive.as( TopologyArchive.class).advertisements() ).contains("foo");
    }
}
