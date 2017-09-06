package org.wildfly.swarm.mpjwtauth.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

public class MPJwtPackageDetector extends PackageFractionDetector {

    public MPJwtPackageDetector() {
        anyPackageOf("org.eclipse.microprofile.annotation");
    }

    @Override
    public String artifactId() {
        return "mpjwtauth";
    }
}
