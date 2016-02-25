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

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class FractionUsageAnalyzerTest {

    @Test
    public void testFractionMatching() throws Exception {
        final Properties properties =
                PropertiesUtil.loadProperties(FractionUsageAnalyzer.class
                                                      .getResourceAsStream("/org/wildfly/swarm/tools/fraction-packages.properties"));

        properties.stringPropertyNames().forEach(fraction -> {
            final Set<String> packages = Stream.of(properties.getProperty(fraction).split(","))
                    .flatMap(pkgs -> Stream.of(pkgs.split("\\+")))
                    .map(p -> p.endsWith("*") ? p.substring(0, p.length() - 1) + ".foo" : p)
                    .collect(Collectors.toSet());

            assertThat(FractionUsageAnalyzer.findFractions(packages)).contains(fraction);
        });
    }

}
