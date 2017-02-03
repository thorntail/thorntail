package org.wildfly.swarm.infinispan.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class InfinispanPackageDetector extends PackageFractionDetector {

    public InfinispanPackageDetector() {
        anyPackageOf("org.infinispan");
    }

    @Override
    public String artifactId() {
        return "infinispan";
    }
}
