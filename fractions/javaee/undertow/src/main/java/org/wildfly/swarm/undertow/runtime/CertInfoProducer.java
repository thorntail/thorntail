package org.wildfly.swarm.undertow.runtime;

import java.io.File;
import java.io.IOException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.descriptors.CertInfo;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CertInfoProducer {

    @Inject
    @Any
    private Instance<UndertowFraction> undertowInstance;

    @Inject
    @ConfigurationValue(SwarmProperties.HTTPS_GENERATE_SELF_SIGNED_CERTIFICATE)
    private Boolean generateSelfCertificate;

    @Inject
    @ConfigurationValue(SwarmProperties.HTTPS_GENERATE_SELF_SIGNED_CERTIFICATE_HOST)
    private String selfCertificateHost;

    @Produces
    @Singleton
    public CertInfo produceCertInfo() {
        if (undertowInstance.isUnsatisfied()) {
            return CertInfo.INVALID;
        }
        UndertowFraction undertowFraction = undertowInstance.get();

        if (generateSelfCertificate != null && generateSelfCertificate) {
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
            return new CertInfo(selfCertificateHost, "jboss.server.data.dir");

        } else {
            String keystorePath = undertowFraction.keystorePath();
            String keystorePassword = undertowFraction.keystorePassword();
            String keyPassword = undertowFraction.keyPassword();
            String keystoreAlias = undertowFraction.alias();
            return new CertInfo(keystorePath, keystorePassword, keyPassword, keystoreAlias);
        }
    }
}
