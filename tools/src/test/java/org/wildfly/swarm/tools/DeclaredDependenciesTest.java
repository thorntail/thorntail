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
package org.wildfly.swarm.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class DeclaredDependenciesTest {

    private static final String COMPILE = "compile";
    private static final String PROVIDED = "provided";
    private static final String TEST = "test";

    private DeclaredDependencies declaredDependencies = new DeclaredDependencies();

    @Test
    public void testUnsolvedDirect() {
        dependencyStream().forEach(declaredDependencies::add);

        Collection<ArtifactSpec> directDependencies = declaredDependencies.getDirectDependencies(true, false);
        Iterator<ArtifactSpec> iterator = directDependencies.iterator();

        assertOrder(iterator);
    }

    private ArtifactSpec createSpec(String gav, String scope) {
        String[] parts = gav.split(":");
        return new ArtifactSpec(scope, parts[0], parts[1], parts[2], "jar", null, null);
    }

    private Stream<ArtifactSpec> dependencyStream() {
        ArrayList<ArtifactSpec> list = new ArrayList<>();
        list.add(createSpec("g:a:1.0", COMPILE));
        list.add(createSpec("g:a:3.0", PROVIDED));
        list.add(createSpec("g:a:2.0", TEST));
        list.add(createSpec("g:a:5.0", TEST));
        list.add(createSpec("g:a:4.0", PROVIDED));
        list.add(createSpec("g:a:6.0", TEST));
        list.add(createSpec("g:a:7.0", COMPILE));
        list.add(createSpec("g:a:8.0", TEST));
        return list.stream();
    }

    private void assertOrder(Iterator<ArtifactSpec> iterator) {
        // compile and provided scopes first
        Assert.assertEquals("1.0", iterator.next().version());
        Assert.assertEquals("3.0", iterator.next().version());
        Assert.assertEquals("4.0", iterator.next().version());
        Assert.assertEquals("7.0", iterator.next().version());
        // test scope second
        Assert.assertEquals("2.0", iterator.next().version());
        Assert.assertEquals("5.0", iterator.next().version());
        Assert.assertEquals("6.0", iterator.next().version());
        Assert.assertEquals("8.0", iterator.next().version());
    }
}
