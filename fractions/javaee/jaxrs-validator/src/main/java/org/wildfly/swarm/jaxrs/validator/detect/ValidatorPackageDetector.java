package org.wildfly.swarm.jaxrs.validator.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class ValidatorPackageDetector extends PackageFractionDetector {

    public ValidatorPackageDetector() {
        allPackages("javax.ws.rs", "javax.validation");
    }

    @Override
    public String artifactId() {
        return "jaxrs-validator";
    }
}
