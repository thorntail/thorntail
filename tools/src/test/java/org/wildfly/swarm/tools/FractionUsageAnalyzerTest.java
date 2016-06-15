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
package org.wildfly.swarm.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class FractionUsageAnalyzerTest {

    @Test
    public void testFractionMatching() throws Exception {
        final Map<String, String> specs = loadPackageSpecs();

        specs.forEach((name, spec) -> {
            final Set<String> potentialMatches = Stream.of(spec.split(","))
                    .flatMap(pkgs -> Stream.of(pkgs.split("\\+")))
                    .map(p -> p.endsWith("*") ? p.substring(0, p.length() - 1) + ".foo" : p)
                    .collect(Collectors.toSet());
            assertThat(new FractionUsageAnalyzer(fractionList())
                               .findFractions(potentialMatches))
                    .contains(new FractionDescriptor("org.wildfly.swarm", name, "0"));

        });
    }

    private static Map<String, String> loadPackageSpecs() {
        try {
            final InputStream in = FractionList.class.getClassLoader()
                    .getResourceAsStream("fraction-packages.properties");

            return new HashMap<>((Map) PropertiesUtil.loadProperties(in));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load fraction-packages.properties", e);
        }

    }

    FractionList fractionList() {
        return new FractionList() {
            @Override
            public Collection<FractionDescriptor> getFractionDescriptors() {
                return null;
            }

            @Override
            public FractionDescriptor getFractionDescriptor(final String groupId, final String artifactId) {
                return null;
            }

            @Override
            public Map<String, FractionDescriptor> getPackageSpecs() {
                final Map<String, String> packageSpecs = loadPackageSpecs();

                return packageSpecs.keySet().stream()
                        .collect(Collectors.toMap(packageSpecs::get,
                                                  f -> new FractionDescriptor("org.wildfly.swarm", f, "0")));
            }
        };
    }
}
