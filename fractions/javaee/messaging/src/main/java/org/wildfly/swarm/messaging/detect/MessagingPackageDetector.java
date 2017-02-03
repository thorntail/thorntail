package org.wildfly.swarm.messaging.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class MessagingPackageDetector extends PackageFractionDetector {

    public MessagingPackageDetector() {
        super();
    }

    @Override
    public String artifactId() {
        return "messaging";
    }
}
