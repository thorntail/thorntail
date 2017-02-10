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
