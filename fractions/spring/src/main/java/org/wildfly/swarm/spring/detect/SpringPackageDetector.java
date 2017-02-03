package org.wildfly.swarm.spring.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class SpringPackageDetector extends PackageFractionDetector {

    public SpringPackageDetector() {
        anyPackageOf("org.springframework");
    }

    @Override
    public String artifactId() {
        return "spring";
    }
}
