package org.wildfly.swarm.undertow.descriptors;

import org.wildfly.swarm.undertow.UndertowProperties;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CertInfo {

    public static final CertInfo INVALID = new CertInfo(null, null, null, null);

    public String keystorePath() {
        return keystorePath;
    }

    public String keystorePassword() {
        return keystorePassword;
    }

    public String keyPassword() {
        return keyPassword;
    }

    public String keystoreAlias() {
        return keystoreAlias;
    }

    public String generateSelfSignedCertificateHost() {
        return generateSelfSignedCertificateHost;
    }

    public String keystoreRelativeTo() {
        return keystoreRelativeTo;
    }

    public CertInfo(String keystorePath, String keystorePassword, String keyPassword, String keystoreAlias) {
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.keyPassword = keyPassword;
        this.keystoreAlias = keystoreAlias;
        this.generateSelfSignedCertificateHost = null;
        this.keystoreRelativeTo = null;
    }

    public CertInfo(String generateSelfSignedCertificateHost, String keystoreRelativeTo) {
        this.keystorePath = UndertowProperties.DEFAULT_KEYSTORE_PATH;
        this.keystorePassword = UndertowProperties.DEFAULT_KEYSTORE_PASSWORD;
        this.keystoreAlias = UndertowProperties.DEFAULT_CERTIFICATE_ALIAS;
        this.keyPassword = UndertowProperties.DEFAULT_KEY_PASSWORD;

        this.generateSelfSignedCertificateHost = generateSelfSignedCertificateHost == null ? "localhost" : generateSelfSignedCertificateHost;
        this.keystoreRelativeTo = keystoreRelativeTo;
    }

    private final String keystorePath;

    private final String keystorePassword;

    private final String keyPassword;

    private final String keystoreAlias;

    private final String keystoreRelativeTo;

    private final String generateSelfSignedCertificateHost;


    public boolean isValid() {
        return ((keystorePath != null && keystorePassword != null && keystoreAlias != null) || generateSelfSignedCertificateHost != null);
    }
}
