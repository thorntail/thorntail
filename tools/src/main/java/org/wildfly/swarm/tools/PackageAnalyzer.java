package org.wildfly.swarm.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.wildfly.swarm.tools.PackageDetector;

/**
 * @author Bob McWhirter
 */
public class PackageAnalyzer {

    private final File source;

    public PackageAnalyzer(File source) {
        this.source = source;
    }

    private Map<String, Set<String>> fractionPackages() throws IOException {
        final Properties properties = new Properties();
        try (InputStream in =
                     PackageAnalyzer.class.getResourceAsStream("/org/wildfly/swarm/tools/fraction-packages.properties")) {
            if (in == null) {
                throw new RuntimeException("Failed to load fraction-packages.properties");
            }
            properties.load(in);
        }

        final Map<String, Set<String>> fractionMap = new HashMap<>();

        for (Map.Entry prop : properties.entrySet()) {
            Set<String> packages = new HashSet<>();
            packages.addAll(Arrays.asList(((String) prop.getValue()).split(",")));
            fractionMap.put((String)prop.getKey(), packages);
        }

        return fractionMap;
    }

    public Set<String> detectNeededFractions() throws IOException {
        final Map<String, Set<String>> fractionPackages = fractionPackages();
        final Set<String> detectedPackages = PackageDetector
                .detectPackages( this.source )
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
