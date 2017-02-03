package org.wildfly.swarm.jaxrs.jaxb.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class JAXBPackageDetector extends PackageFractionDetector {

    public JAXBPackageDetector() {
        allPackages("javax.ws.rs", "javax.xml.bind");
    }

    @Override
    public String artifactId() {
        return "jaxrs-jaxb";
    }
}
