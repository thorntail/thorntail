/*
 * Copyright 2015-2019 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.env;

import java.util.Collection;
import java.util.Iterator;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.junit.Assert;
import org.junit.Test;

public class DependencyTreeTest {

    private DependencyTree<ArtifactCoordinates> tree = new DependencyTree<>();

    @Test
    public void testDirectDepsOrder() {
        tree.add(ArtifactCoordinates.fromString("g:a:2.0"));
        tree.add(ArtifactCoordinates.fromString("g:a:1.0"));
        tree.add(ArtifactCoordinates.fromString("g:a:3.0"));
        tree.add(ArtifactCoordinates.fromString("g:a:4.0"));
        tree.add(ArtifactCoordinates.fromString("g:a:5.0"));
        tree.add(ArtifactCoordinates.fromString("g:a:6.0"));
        tree.add(ArtifactCoordinates.fromString("g:a:7.0"));
        tree.add(ArtifactCoordinates.fromString("g:a:1.5"));

        Collection<ArtifactCoordinates> directDeps = tree.getDirectDeps();
        Iterator<ArtifactCoordinates> iterator = directDeps.iterator();

        Assert.assertEquals("2.0", iterator.next().getVersion());
        Assert.assertEquals("1.0", iterator.next().getVersion());
        Assert.assertEquals("3.0", iterator.next().getVersion());
        Assert.assertEquals("4.0", iterator.next().getVersion());
        Assert.assertEquals("5.0", iterator.next().getVersion());
        Assert.assertEquals("6.0", iterator.next().getVersion());
        Assert.assertEquals("7.0", iterator.next().getVersion());
        Assert.assertEquals("1.5", iterator.next().getVersion());
    }

    @Test
    public void testTransitiveDepsOrder() {
        ArtifactCoordinates parent = ArtifactCoordinates.fromString("g:a:v");
        tree.add(parent, ArtifactCoordinates.fromString("g:a:2.0"));
        tree.add(parent, ArtifactCoordinates.fromString("g:a:1.0"));
        tree.add(parent, ArtifactCoordinates.fromString("g:a:3.0"));
        tree.add(parent, ArtifactCoordinates.fromString("g:a:4.0"));
        tree.add(parent, ArtifactCoordinates.fromString("g:a:5.0"));
        tree.add(parent, ArtifactCoordinates.fromString("g:a:6.0"));
        tree.add(parent, ArtifactCoordinates.fromString("g:a:7.0"));
        tree.add(parent, ArtifactCoordinates.fromString("g:a:1.5"));

        Collection<ArtifactCoordinates> transientDeps = tree.getTransientDeps(parent);
        Iterator<ArtifactCoordinates> iterator = transientDeps.iterator();

        Assert.assertEquals("2.0", iterator.next().getVersion());
        Assert.assertEquals("1.0", iterator.next().getVersion());
        Assert.assertEquals("3.0", iterator.next().getVersion());
        Assert.assertEquals("4.0", iterator.next().getVersion());
        Assert.assertEquals("5.0", iterator.next().getVersion());
        Assert.assertEquals("6.0", iterator.next().getVersion());
        Assert.assertEquals("7.0", iterator.next().getVersion());
        Assert.assertEquals("1.5", iterator.next().getVersion());
    }
}
