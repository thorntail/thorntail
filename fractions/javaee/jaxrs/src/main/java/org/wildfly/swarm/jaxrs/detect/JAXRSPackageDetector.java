package org.wildfly.swarm.jaxrs.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class JAXRSPackageDetector extends PackageFractionDetector {

    public JAXRSPackageDetector() {
        anyPackageOf("javax.ws.rs");
    }

    @Override
    public String artifactId() {
        return "jaxrs";
    }
}
