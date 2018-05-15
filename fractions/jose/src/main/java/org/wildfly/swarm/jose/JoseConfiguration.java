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
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_JOSE_FORMAT;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEYSTORE_PASSWORD;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEYSTORE_PATH;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEYSTORE_TYPE;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEY_ALIAS;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEY_ENCRYPTION_ALGORITHM;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_KEY_PASSWORD;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_SIGNATURE_ALGORITHM;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_SIGNATURE_DATA_DETACHED;
import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_SIGNATURE_DATA_ENCODING;
import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.string;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.annotations.Configurable;

public class JoseConfiguration {

    public JoseConfiguration keystorePassword(String password) {
        this.keystorePassword.set(password);
        return this;
    }

    public String keystorePassword() {
        return this.keystorePassword.get();
    }

    public JoseConfiguration signatureKeyPassword(String password) {
        this.signatureKeyPassword.set(password);
        return this;
    }

    public String signatureKeyPassword() {
        return this.signatureKeyPassword.get();
    }

    public JoseConfiguration keystoreType(String type) {
        this.keystoreType.set(type);
        return this;
    }

    public String keystoreType() {
        return this.keystoreType.get();
    }

    public JoseConfiguration keystorePath(String path) {
        this.keystorePath.set(path);
        return this;
    }

    public String keystorePath() {
        return this.keystorePath.get();
    }

    public JoseConfiguration signatureKeyAlias(String keyAlias) {
        this.signatureKeyAlias.set(keyAlias);
        return this;
    }

    public String signatureKeyAlias() {
        return this.signatureKeyAlias.get();
    }

    public JoseConfiguration signatureAlgorithm(String algorithm) {
        signatureAlgorithm.set(algorithm);
        return this;
    }

    public String signatureAlgorithm() {
        return this.signatureAlgorithm.get();
    }

    public JoseConfiguration signatureFormat(JoseFormat format) {
        signatureFormat.set(format.name());
        return this;
    }

    public JoseConfiguration signatureDataEncoding(boolean encoding) {
        signatureDataEncoding.set(encoding);
        return this;
    }

    public boolean signatureDataEncoding() {
        return this.signatureDataEncoding.get();
    }

    public JoseConfiguration signatureDataDetached(boolean detached) {
        signatureDataDetached.set(detached);
        return this;
    }

    public boolean signatureDataDetached() {
        return this.signatureDataDetached.get();
    }

    public JoseFormat signatureFormat() {
        return JoseFormat.valueOf(this.signatureFormat.get());
    }

    public JoseConfiguration encryptionFormat(JoseFormat format) {
        encryptionFormat.set(format.name());
        return this;
    }

    public JoseFormat encryptionFormat() {
        return JoseFormat.valueOf(this.encryptionFormat.get());
    }

    public JoseConfiguration encryptionKeyAlias(String keyAlias) {
        this.encryptionKeyAlias.set(keyAlias);
        return this;
    }

    public String encryptionKeyAlias() {
        return this.encryptionKeyAlias.get();
    }

    public JoseConfiguration encryptionKeyPassword(String password) {
        this.encryptionKeyPassword.set(password);
        return this;
    }

    public String encryptionKeyPassword() {
        return this.encryptionKeyPassword.get();
    }

    public JoseConfiguration keyEncryptionAlgorithm(String algorithm) {
        keyEncryptionAlgorithm.set(algorithm);
        return this;
    }

    public String keyEncryptionAlgorithm() {
        return this.keyEncryptionAlgorithm.get();
    }

    public JoseConfiguration contentEncryptionAlgorithm(String algorithm) {
        contentEncryptionAlgorithm.set(algorithm);
        return this;
    }

    public String contentEncryptionAlgorithm() {
        return this.contentEncryptionAlgorithm.get();
    }
    /**
     * Keystore type.
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
     * Signature algorithm.
     */
    @Configurable("swarm.jose.signature.algorithm")
    @AttributeDocumentation("Signature algorithm")
    private Defaultable<String> signatureAlgorithm = string(DEFAULT_SIGNATURE_ALGORITHM);

    /**
     * Signature Format.
     */
    @Configurable("swarm.jose.signature.format")
    @AttributeDocumentation("Compact or JSON JWS format, support for JSON is optional")
    private Defaultable<String> signatureFormat = string(DEFAULT_JOSE_FORMAT.name());

    /**
     * Signature Data Encoding.
     */
    @Configurable("swarm.jose.signature.data-encoding")
    @AttributeDocumentation("JWS data encoding mode, true - base64url (default), false - clear text")
    private Defaultable<Boolean> signatureDataEncoding = bool(DEFAULT_SIGNATURE_DATA_ENCODING);

    /**
     * Signature Detached Data.
     */
    @Configurable("swarm.jose.signature.data-detached")
    @AttributeDocumentation("JWS data detached mode, true - the data is in the JWS payload (default), false - outside")
    private Defaultable<Boolean> signatureDataDetached = bool(DEFAULT_SIGNATURE_DATA_DETACHED);

    /**
     * Password for the signature key.
     */
    @Configurable("swarm.jose.signature.key.password")
    @AttributeDocumentation("Password to the signature private key")
    private Defaultable<String> signatureKeyPassword = string(DEFAULT_KEY_PASSWORD);

    /**
     * Alias to the signature key entry in the keystore.
     */
    @Configurable("swarm.jose.signature.key.alias")
    @AttributeDocumentation("Alias to the signature key entry in the keystore")
    private Defaultable<String> signatureKeyAlias = string(DEFAULT_KEY_ALIAS);

    /**
     * Encryption Format.
     */
    @Configurable("swarm.jose.encryption.format")
    @AttributeDocumentation("Compact or JSON JWE format, support for JSON is optional")
    private Defaultable<String> encryptionFormat = string(DEFAULT_JOSE_FORMAT.name());

    /**
     * Key Encryption algorithm.
     */
    @Configurable("swarm.jose.encryption.keyAlgorithm")
    @AttributeDocumentation("Key Encryption algorithm")
    private Defaultable<String> keyEncryptionAlgorithm = string(DEFAULT_KEY_ENCRYPTION_ALGORITHM);

    /**
     * Content Encryption algorithm.
     */
    @Configurable("swarm.jose.encryption.contentAlgorithm")
    @AttributeDocumentation("Content Encryption algorithm")
    private Defaultable<String> contentEncryptionAlgorithm = string(DEFAULT_CONTENT_ENCRYPTION_ALGORITHM);

    /**
     * Password for the encryption key.
     */
    @Configurable("swarm.jose.encryption.key.password")
    @AttributeDocumentation("Password to the encryption private key")
    private Defaultable<String> encryptionKeyPassword = string(DEFAULT_KEY_PASSWORD);

    /**
     * Alias to the encryption key entry in the keystore.
     */
    @Configurable("swarm.jose.encryption.key.alias")
    @AttributeDocumentation("Alias to the encryption key entry in the keystore")
    private Defaultable<String> encryptionKeyAlias = string(DEFAULT_KEY_ALIAS);
}
