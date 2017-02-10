package org.wildfly.swarm.swagger.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class SwaggerPackageDetector extends PackageFractionDetector {

    public SwaggerPackageDetector() {
        anyPackageOf("io.swagger.annotations");
    }

    @Override
    public String artifactId() {
        return "swagger";
    }
}
