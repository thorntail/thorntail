package org.wildfly.swarm.jaxrs.jsonp.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class JsonPPackageDetector extends PackageFractionDetector {

    public JsonPPackageDetector() {
        allPackages("javax.ws.rs", "javax.json");
    }

    @Override
    public String artifactId() {
        return "jaxrs-jsonp";
    }
}
