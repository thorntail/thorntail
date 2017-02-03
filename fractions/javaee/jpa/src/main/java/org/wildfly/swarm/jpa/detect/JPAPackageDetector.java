package org.wildfly.swarm.jpa.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class JPAPackageDetector extends PackageFractionDetector {

    public JPAPackageDetector() {
        anyPackageOf("javax.persistence");
    }

    @Override
    public String artifactId() {
        return "jpa";
    }
}
