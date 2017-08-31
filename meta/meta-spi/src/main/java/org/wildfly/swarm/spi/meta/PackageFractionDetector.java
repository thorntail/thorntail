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
package org.wildfly.swarm.spi.meta;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Ken Finnigan
 */
public abstract class PackageFractionDetector implements FractionDetector<String> {

    @Override
    public String extensionToDetect() {
        return "class";
    }

    @Override
    public boolean detectionComplete() {
        return detectionComplete;
    }

    @Override
    public boolean wasDetected() {
        return detected;
    }

    @Override
    public void detect(String element) {
        if (!detectionComplete() && element != null) {
            detected = anyPackages.stream()
                    .anyMatch(element::startsWith);

            if (detected) {
                detectionComplete = true;
                return;
            }

            detected = anyClasses.stream()
                    .anyMatch(element::equals);

            if (detected) {
                detectionComplete = true;
                return;
            }

            if (allPackages.size() > 0) {
                allPackages.entrySet()
                        .stream()
                        .filter(e -> element.startsWith(e.getKey()))
                        .forEach(e -> e.setValue(Boolean.TRUE));

                long found = allPackages.values()
                        .stream()
                        .filter(v -> v.equals(Boolean.TRUE))
                        .count();

                if (found == allPackages.size()) {
                    detected = true;
                    detectionComplete = true;
                }
            }
        }
    }

    public void anyPackageOf(String... packages) {
        Collections.addAll(this.anyPackages, packages);
    }

    public void anyClassOf(String... classes) {
        Collections.addAll(this.anyClasses, classes);
    }

    public void allPackages(String... packages) {
        for (String pkg : packages) {
            allPackages.put(pkg, Boolean.FALSE);
        }
    }

    private boolean detected = false;

    private boolean detectionComplete = false;

    private Collection<String> anyPackages = new HashSet<>();

    private Collection<String> anyClasses = new HashSet<>();

    private Map<String, Boolean> allPackages = new HashMap<>();
}
