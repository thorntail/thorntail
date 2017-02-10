package org.wildfly.swarm.ejb.remote.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class EJBRemotePackageDetector extends PackageFractionDetector {

    public EJBRemotePackageDetector() {
        anyClassOf("javax.ejb.Remote");
    }

    @Override
    public String artifactId() {
        return "ejb-remote";
    }
}
