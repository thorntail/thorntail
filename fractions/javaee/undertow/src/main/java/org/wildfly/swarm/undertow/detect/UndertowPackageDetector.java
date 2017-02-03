package org.wildfly.swarm.undertow.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class UndertowPackageDetector extends PackageFractionDetector {

    public UndertowPackageDetector() {
        anyPackageOf("javax.servlet", "javax.websocket", "io.undertow");
    }

    @Override
    public String artifactId() {
        return "undertow";
    }
}
