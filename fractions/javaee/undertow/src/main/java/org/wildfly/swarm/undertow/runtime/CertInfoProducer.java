package org.wildfly.swarm.undertow.runtime;

import java.io.File;
import java.io.IOException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.descriptors.CertInfo;

import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.string;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CertInfoProducer {

    @Inject
    UndertowFraction undertow;

    @Configurable("swarm.https.certificate.generate")
    Defaultable<Boolean> generateSelfCertificate = bool(false);

    @Configurable( "swarm.https.certificate.generate.host")
    Defaultable<String> selfCertificateHost = string("localhost");

    @Produces
    @Singleton
    public CertInfo produceCertInfo() {
        if (generateSelfCertificate.get()) {
            // Remove when SWARM-634 is fixed
            if (System.getProperty("jboss.server.data.dir") == null) {
                File tmpDir = null;
                try {
                    tmpDir = TempFileManager.INSTANCE.newTempDirectory("wildfly-swarm-data", ".d");
                    System.setProperty("jboss.server.data.dir", tmpDir.getAbsolutePath());
                } catch (IOException e) {
                    // Ignore
                }
            }
            return new CertInfo(selfCertificateHost.get(), "jboss.server.data.dir");

        } else {
            String keystorePath = undertow.keystorePath();
            String keystorePassword = undertow.keystorePassword();
            String keyPassword = undertow.keyPassword();
            String keystoreAlias = undertow.alias();
            return new CertInfo(keystorePath, keystorePassword, keyPassword, keystoreAlias);
        }
    }
}
