package org.wildfly.swarm.mail.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class MailPackageDetector extends PackageFractionDetector {

    public MailPackageDetector() {
        anyPackageOf("javax.mail");
    }

    @Override
    public String artifactId() {
        return "mail";
    }
}
