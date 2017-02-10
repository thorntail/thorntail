package org.wildfly.swarm.cdi.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class CDIPackageDetector extends PackageFractionDetector {

    public CDIPackageDetector() {
        anyPackageOf("javax.inject", "javax.enterprise.inject", "javax.enterprise.context", "javax.enterprise.event");
    }

    @Override
    public String artifactId() {
        return "cdi";
    }
}
