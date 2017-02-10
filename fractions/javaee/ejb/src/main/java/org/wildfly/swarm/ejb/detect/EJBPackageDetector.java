package org.wildfly.swarm.ejb.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class EJBPackageDetector extends PackageFractionDetector {

    public EJBPackageDetector() {
        anyPackageOf("javax.ejb");
    }

    @Override
    public String artifactId() {
        return "ejb";
    }
}
