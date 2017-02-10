package org.wildfly.swarm.javafx.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class JavaFXPackageDetector extends PackageFractionDetector {

    public JavaFXPackageDetector() {
        anyPackageOf("javafx");
    }

    @Override
    public String artifactId() {
        return "javafx";
    }
}
