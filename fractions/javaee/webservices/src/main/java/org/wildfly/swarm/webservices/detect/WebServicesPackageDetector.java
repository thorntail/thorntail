package org.wildfly.swarm.webservices.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class WebServicesPackageDetector extends PackageFractionDetector {

    public WebServicesPackageDetector() {
        anyPackageOf("javax.jws", "javax.xml.ws");
    }

    @Override
    public String artifactId() {
        return "webservices";
    }
}
