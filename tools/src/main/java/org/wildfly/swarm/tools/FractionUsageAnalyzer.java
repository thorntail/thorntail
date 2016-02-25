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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 * @author Toby Crawley
 */
public class FractionUsageAnalyzer {

    private final File source;

    public FractionUsageAnalyzer(Path source) {
        this(source.toFile());
    }

    public FractionUsageAnalyzer(File source) {
        this.source = source;
    }


    public Set<String> detectNeededFractions() throws IOException {
        return findFractions(PackageDetector
                                     .detectPackages(this.source)
                                     .keySet());
    }

    static protected Set<String> findFractions(Set<String> packages) {
        final Set<StatefulPackageMatcher> fractionPackages = fractionMatchers();

        return packages.stream()
                .flatMap(p -> fractionPackages.stream().map(m -> m.fraction(p)))
                .filter(p -> p != null)
                .collect(Collectors.toSet());
    }

    static private Set<StatefulPackageMatcher> fractionMatchers() {
        final Properties properties;
        try {
            properties = PropertiesUtil.loadProperties(FractionUsageAnalyzer.class
                                                               .getResourceAsStream("/org/wildfly/swarm/tools/fraction-packages.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fraction-packages.properties", e);
        }

        return properties.stringPropertyNames().stream()
                .flatMap(fractionName ->
                                 Stream.of(properties.getProperty(fractionName).split(","))
                                         .map(packages -> new StatefulPackageMatcher(fractionName,
                                                                                     packages.split("\\+"))))
                .collect(Collectors.toSet());
    }


    static class StatefulPackageMatcher {
        StatefulPackageMatcher(String fractionName, String... packages) {
            this.fractionName = fractionName;
            this.packageSpecs.addAll(Arrays.asList(packages));
        }

        /**
         * Returns the fraction name for the given package.
         * If the matcher requires multiple packages for a fraction, the last matching package
         * will cause the fraction name to be returned.
         * @param pkg the package to match against
         * @return the matching fraction name or null
         */
        public String fraction(final String pkg) {
            final String match = matchingSpec(pkg);
            if (match != null) {
                this.matchedPackages.add(match);
            }

            return this.matchedPackages.equals(packageSpecs) ? this.fractionName : null;
        }

        private String matchingSpec(final String pkg) {
            return this.packageSpecs.stream()
                    .filter(spec -> {
                        if (spec.endsWith("*")) {
                            return pkg.startsWith(spec.substring(0, spec.length() - 1));
                        } else {
                            return pkg.equals(spec);
                        }
                    })
                    .findFirst()
                    .orElse(null);
        }

        private final String fractionName;
        private final Set<String> packageSpecs = new HashSet<>();
        private final Set<String> matchedPackages = new HashSet<>();
    }
}
