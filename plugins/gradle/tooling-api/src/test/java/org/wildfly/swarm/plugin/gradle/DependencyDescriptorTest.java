/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wildfly.swarm.plugin.gradle;

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.swarm.tools.ArtifactSpec;

public class DependencyDescriptorTest {

    /**
     * The dependency descriptor will be serialized and consumed between the Gradle daemon and the Arquillian adapter. This
     * test ensures that the basics are working fine.
     */
    @Test
    public void verifyEqualsAndHashCode() {
        DependencyDescriptor dd1 =
                new DefaultDependencyDescriptor("compile", "org.wildfly.swarm", "test-artifact", "0.0.0", "jar", null, null);
        DependencyDescriptor dd2 =
                new DefaultDependencyDescriptor("compile", "org.wildfly.swarm", "test-artifact", "0.0.0", "jar", null, null);

        Assert.assertEquals("Equals method doesn't seem to be working.", dd1, dd2);
        Assert.assertEquals("Hashcode method doesn't seem to be working.", dd1.hashCode(), dd2.hashCode());

        // Convert it to an artifact-spec and then back again and compare.
        ArtifactSpec a1 = GradleToolingHelper.toArtifactSpec(dd1);
        dd2 = new DefaultDependencyDescriptor(a1);
        Assert.assertEquals("Equals method doesn't seem to be working.", dd1, dd2);
        Assert.assertEquals("Hashcode method doesn't seem to be working.", dd1.hashCode(), dd2.hashCode());
    }
}
