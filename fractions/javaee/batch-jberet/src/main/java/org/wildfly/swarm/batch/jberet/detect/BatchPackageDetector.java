package org.wildfly.swarm.batch.jberet.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class BatchPackageDetector extends PackageFractionDetector {

    public BatchPackageDetector() {
        anyPackageOf("javax.batch");
    }

    @Override
    public String artifactId() {
        return "batch-jberet";
    }
}
