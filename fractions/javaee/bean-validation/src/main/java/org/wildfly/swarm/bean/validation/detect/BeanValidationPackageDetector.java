package org.wildfly.swarm.bean.validation.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class BeanValidationPackageDetector extends PackageFractionDetector {

    public BeanValidationPackageDetector() {
        anyPackageOf("javax.validation");
    }

    @Override
    public String artifactId() {
        return "bean-validation";
    }
}
