package org.wildfly.swarm.datasources.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class DatasourcesPackageDetector extends PackageFractionDetector {

    public DatasourcesPackageDetector() {
        anyPackageOf("javax.sql");
    }

    @Override
    public String artifactId() {
        return "datasources";
    }
}
