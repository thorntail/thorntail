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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmBootstrapConfTest {

    @Test
    public void testRoundTripWriteRead() throws Exception {

        WildFlySwarmBootstrapConf bootstrapConf = new WildFlySwarmBootstrapConf();

        bootstrapConf.addEntry("org.wildfly.swarm:dog:1.0");
        bootstrapConf.addEntry("org.wildfly.swarm:cat:1.0");
        bootstrapConf.addEntry("org.wildfly.swarm:fish:1.0:redhat-1");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        bootstrapConf.write(out);

        String contents = new String(out.toByteArray());

        String[] lines = contents.split("(\\r)?(\\n)");

        assertThat(lines).hasSize(3);

        assertThat(lines).contains("org.wildfly.swarm:dog:1.0");
        assertThat(lines).contains("org.wildfly.swarm:cat:1.0");
        assertThat(lines).contains("org.wildfly.swarm:fish:1.0:redhat-1");

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        bootstrapConf = new WildFlySwarmBootstrapConf(in);

        assertThat(bootstrapConf.getEntries()).hasSize(3);

        assertContains(bootstrapConf, "org.wildfly.swarm", "dog", "1.0", null);
        assertContains(bootstrapConf, "org.wildfly.swarm", "cat", "1.0", null);
        assertContains(bootstrapConf, "org.wildfly.swarm", "fish", "1.0", "redhat-1");
    }

    @Test
    public void testRoundTripReadWrite() throws Exception {

        StringBuffer txt = new StringBuffer();

        txt.append("org.wildfly.swarm:dog:1.0\n");
        txt.append("org.wildfly.swarm:cat:1.0\n");
        txt.append("org.wildfly.swarm:fish:1.0:redhat-1\n");

        ByteArrayInputStream in = new ByteArrayInputStream(txt.toString().getBytes());

        WildFlySwarmBootstrapConf bootstrapConf = new WildFlySwarmBootstrapConf(in);

        assertThat(bootstrapConf.getEntries()).hasSize(3);

        assertContains(bootstrapConf, "org.wildfly.swarm", "dog", "1.0", null);
        assertContains(bootstrapConf, "org.wildfly.swarm", "cat", "1.0", null);
        assertContains(bootstrapConf, "org.wildfly.swarm", "fish", "1.0", "redhat-1");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        bootstrapConf.write(out);

        String written = new String(out.toByteArray());

        String[] lines = written.split("(\\r)?(\\n)");

        assertThat(lines).hasSize(3);

        assertThat(lines).contains("org.wildfly.swarm:dog:1.0");
        assertThat(lines).contains("org.wildfly.swarm:cat:1.0");
        assertThat(lines).contains("org.wildfly.swarm:fish:1.0:redhat-1");

    }

    protected void assertContains(WildFlySwarmBootstrapConf conf, String groupId, String artifactId, String version, String classifier) {
        MavenArtifactDescriptor expected = MavenArtifactDescriptor.build().groupId(groupId).artifactId(artifactId).version(version).classifier(classifier).build();
        assertThat(conf.getEntries()).contains(expected);
    }

}
