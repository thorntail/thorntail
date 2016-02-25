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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

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

    private Map<String, Set<String>> fractionPackages() {
        final Properties properties = new Properties();
        try (InputStream in =
                     FractionUsageAnalyzer.class.getResourceAsStream("/org/wildfly/swarm/tools/fraction-packages.properties")) {
            properties.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fraction-packages.properties", e);
        }

        final Map<String, Set<String>> fractionMap = new HashMap<>();

        for (Map.Entry prop : properties.entrySet()) {
            Set<String> packages = new HashSet<>();
            packages.addAll(Arrays.asList(((String) prop.getValue()).split(",")));
            fractionMap.put((String) prop.getKey(), packages);
        }

        return fractionMap;
    }

    public Set<String> detectNeededFractions() throws IOException {
        final Map<String, Set<String>> fractionPackages = fractionPackages();
        final Set<String> detectedPackages = PackageDetector
                .detectPackages(this.source)
                .keySet();
        final Set<String> neededFractions = new HashSet<>();

        for (Map.Entry<String, Set<String>> fraction : fractionPackages.entrySet()) {
            neededFractions.addAll(fraction.getValue()
                    .stream()
                    .filter(detectedPackages::contains)
                    .map(pkg -> fraction.getKey())
                    .collect(Collectors.toList()));
        }

        return neededFractions;
    }
}
