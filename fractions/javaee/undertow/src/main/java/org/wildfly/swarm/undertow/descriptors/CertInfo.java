/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        return ((keystorePath != null && keystorePassword != null) || generateSelfSignedCertificateHost != null);
    }
}
