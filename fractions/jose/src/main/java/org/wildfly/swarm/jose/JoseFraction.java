/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.jose;

import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEYSTORE_PASSWORD;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEYSTORE_PATH;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEYSTORE_TYPE;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEY_ALIAS;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEY_ENCRYPTION_ALGORITHM;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEY_PASSWORD;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_SIGNATURE_ALGORITHM;
import static org.wildfly.swarm.spi.api.Defaultable.string;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;

@DeploymentModules({
    @DeploymentModule(name = "org.wildfly.swarm.jose.provider"),
    @DeploymentModule(name = "org.wildfly.swarm.jose",
                      slot = "deployment",
                      export = true,
                      metaInf = DeploymentModule.MetaInfDisposition.IMPORT)
})
public class JoseFraction implements Fraction<JoseFraction> {

    public Jose getJoseInstance() {
        return new DefaultJoseImpl(this);
    }

    public JoseFraction keystorePassword(String password) {
        this.keystorePassword.set(password);
        return this;
    }

    public String keystorePassword() {
        return this.keystorePassword.get();
    }

    public JoseFraction keyPassword(String password) {
        this.keyPassword.set(password);
        return this;
    }

    public String keyPassword() {
        return this.keyPassword.get();
    }

    public JoseFraction keystoreType(String type) {
        this.keystoreType.set(type);
        return this;
    }

    public String keystoreType() {
        return this.keystoreType.get();
    }

    public JoseFraction keystorePath(String path) {
        this.keystorePath.set(path);
        return this;
    }

    public String keystorePath() {
        return this.keystorePath.get();
    }

    public JoseFraction keyAlias(String keyAlias) {
        this.keyAlias.set(keyAlias);
        return this;
    }

    public String keyAlias() {
        return this.keyAlias.get();
    }

    public JoseFraction signatureAlgorithm(String algorithm) {
        signatureAlgorithm.set(algorithm);
        return this;
    }

    public String signatureAlgorithm() {
        return this.signatureAlgorithm.get();
    }

    public JoseFraction keyEncryptionAlgorithm(String algorithm) {
        keyEncryptionAlgorithm.set(algorithm);
        return this;
    }

    public String keyEncryptionAlgorithm() {
        return this.keyEncryptionAlgorithm.get();
    }

    public JoseFraction contentEncryptionAlgorithm(String algorithm) {
        contentEncryptionAlgorithm.set(algorithm);
        return this;
    }

    public String contentEncryptionAlgorithm() {
        return this.contentEncryptionAlgorithm.get();
    }
    /**
     * Path to the keystore.
     */
    @Configurable("swarm.jose.keystore.type")
    @AttributeDocumentation("Keystore type")
    private Defaultable<String> keystoreType = string(DEFAULT_KEYSTORE_TYPE);

    /**
     * Path to the keystore.
     */
    @Configurable("swarm.jose.keystore.path")
    @AttributeDocumentation("Path to the keystore")
    private Defaultable<String> keystorePath = string(DEFAULT_KEYSTORE_PATH);

    /**
     * Password for the keystore.
     */
    @Configurable("swarm.jose.keystore.password")
    @AttributeDocumentation("Password to the keystore")
    private Defaultable<String> keystorePassword = string(DEFAULT_KEYSTORE_PASSWORD);

    /**
     * Password for the key.
     */
    @Configurable("swarm.jose.key.password")
    @AttributeDocumentation("Password to the private key")
    private Defaultable<String> keyPassword = string(DEFAULT_KEY_PASSWORD);

    /**
     * Alias to the key entry in the keystore.
     */
    @Configurable("swarm.jose.key.alias")
    @AttributeDocumentation("Alias to the key entry in the keystore")
    private Defaultable<String> keyAlias = string(DEFAULT_KEY_ALIAS);

    /**
     * Signature algorithm.
     */
    @Configurable("swarm.jose.signature.algorithm")
    @AttributeDocumentation("Signature algorithm")
    private Defaultable<String> signatureAlgorithm = string(DEFAULT_SIGNATURE_ALGORITHM);

    /**
     * Key Encryption algorithm.
     */
    @Configurable("swarm.jose.encryption.keyalgorithm")
    @AttributeDocumentation("Key Encryption algorithm")
    private Defaultable<String> keyEncryptionAlgorithm = string(DEFAULT_KEY_ENCRYPTION_ALGORITHM);

    /**
     * Content Encryption algorithm.
     */
    @Configurable("swarm.jose.encryption.contentalgorithm")
    @AttributeDocumentation("Content Encryption algorithm")
    private Defaultable<String> contentEncryptionAlgorithm = string(DEFAULT_CONTENT_ENCRYPTION_ALGORITHM);
}