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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 * @author Toby Crawley
 */
public class FractionUsageAnalyzer {
    public FractionUsageAnalyzer(final FractionList fractionList) {
        this.fractionList = fractionList;
    }

    public FractionUsageAnalyzer source(final Path source) {
        source(source.toFile());

        return this;
    }

    public FractionUsageAnalyzer source(final File source) {
        this.sources.add(source);

        return this;
    }

    public Set<FractionDescriptor> detectNeededFractions() throws IOException {
        if (this.fractionList == null) {
            return Collections.emptySet();
        }
        final Set<FractionDescriptor> specs = new HashSet<>();
        for (File f : this.sources) {
            specs.addAll(findFractions(PackageDetector
                                               .detectPackages(f)
                                               .keySet()));
        }

        // Remove fractions that have a dependency on each other
        Iterator<FractionDescriptor> it = specs.iterator();
        while (it.hasNext()) {
            FractionDescriptor descriptor = it.next();
            // Is set as a dependency to any other descriptor? If so, remove it
            if (specs.stream().anyMatch(fd->fd.getDependencies().contains(descriptor))) {
                it.remove();
            }
        }
        // Add container only if no fractions are detected, as they have a transitive dependency to container
        if (specs.isEmpty()) {
            specs.add(this.fractionList.getFractionDescriptor(DependencyManager.WILDFLY_SWARM_GROUP_ID, "container"));
        }
        return specs;
    }

    protected Set<FractionDescriptor> findFractions(Set<String> packages) {
        final List<StatefulPackageMatcher> fractionPackages = fractionMatchers();

        return packages.stream()
                .flatMap(p -> fractionPackages.stream().map(m -> m.fraction(p)))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<StatefulPackageMatcher> fractionMatchers() {
        final List<StatefulPackageMatcher> matchers = new ArrayList<>();

        this.fractionList.getPackageSpecs().forEach((spec, fd) ->
                                                            Stream.of(spec.split(","))
                                                                    .forEach(s -> matchers.add(new StatefulPackageMatcher(fd,
                                                                                                                          s.split("\\+")))));

        return matchers;
    }

    private final List<File> sources = new ArrayList<>();

    private final FractionList fractionList;


    private static class StatefulPackageMatcher {
        StatefulPackageMatcher(FractionDescriptor desc, String... packages) {
            this.descriptor = desc;
            this.packageSpecs = new HashSet<>(Arrays.asList(packages));
        }

        /**
         * Returns the fraction name for the given package.
         * If the matcher requires multiple packages for a fraction, the last matching package
         * will cause the fraction name to be returned.
         *
         * @param pkg the package to match against
         * @return the matching fraction descriptor or null
         */
        public FractionDescriptor fraction(final String pkg) {
            final String match = matchingSpec(pkg);
            if (match != null) {
                this.matchedPackages.add(match);
            }

            return this.matchedPackages.equals(packageSpecs) ? this.descriptor : null;
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

        private final FractionDescriptor descriptor;

        private final Set<String> packageSpecs;

        private final Set<String> matchedPackages = new HashSet<>();
    }
}
