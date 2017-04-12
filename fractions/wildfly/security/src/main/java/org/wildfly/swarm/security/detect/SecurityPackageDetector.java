package org.wildfly.swarm.security.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * Created by bob on 4/12/17.
 */
public class SecurityPackageDetector extends PackageFractionDetector {

    public SecurityPackageDetector() {
        anyPackageOf("javax.annotation.security", "javax.security");
    }

    @Override
    public String artifactId() {
        return "security";
    }
}
