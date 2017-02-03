package org.wildfly.swarm.jsf.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class JSFPackageDetector extends PackageFractionDetector {

    public JSFPackageDetector() {
        anyPackageOf("javax.faces");
    }

    @Override
    public String artifactId() {
        return "jsf";
    }
}
