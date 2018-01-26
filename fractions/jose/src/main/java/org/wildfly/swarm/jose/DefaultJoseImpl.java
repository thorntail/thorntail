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

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.rs.security.jose.common.JoseConstants;
import org.apache.cxf.rs.security.jose.jwe.JweCompactConsumer;
import org.apache.cxf.rs.security.jose.jwe.JweCompactProducer;
import org.apache.cxf.rs.security.jose.jwe.JweDecryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.JweEncryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.JweHeaders;
import org.apache.cxf.rs.security.jose.jwe.JweUtils;
import org.apache.cxf.rs.security.jose.jws.JwsCompactConsumer;
import org.apache.cxf.rs.security.jose.jws.JwsCompactProducer;
import org.apache.cxf.rs.security.jose.jws.JwsHeaders;
import org.apache.cxf.rs.security.jose.jws.JwsSignatureProvider;
import org.apache.cxf.rs.security.jose.jws.JwsSignatureVerifier;
import org.apache.cxf.rs.security.jose.jws.JwsUtils;

public class DefaultJoseImpl implements Jose {
    private JoseFraction fraction;
    public DefaultJoseImpl(JoseFraction fraction) {
        this.fraction = fraction;
    }

    @Override
    public String sign(String data) {
        return sign(Collections.emptyMap(), data);
    }

    @Override
    public String sign(Map<String, Object> metadata, String data) {
        JwsHeaders headers = new JwsHeaders();
        headers.asMap().putAll(metadata);
        Properties props = prepareSignatureProperties();

        try {
            JwsSignatureProvider provider = JwsUtils.loadSignatureProvider(props, headers);
            JwsCompactProducer producer = new JwsCompactProducer(headers, data);
            return producer.signWith(provider);
        } catch (Exception ex) {
            throw new JoseException("Signature Creation Failure");
        }
    }

    @Override
    public String verify(String signedData) throws JoseException {
        return verification(signedData).getData();
    }

    @Override
    public VerifiedData verification(String signedData) throws JoseException {
        Properties props = prepareSignatureProperties();

        try {
            JwsCompactConsumer consumer = new JwsCompactConsumer(signedData);
            JwsSignatureVerifier verifier = JwsUtils.loadSignatureVerifier(props, consumer.getJwsHeaders());
            if (!consumer.verifySignatureWith(verifier)) {
                throw new JoseException("Jose Signature Verification Failure");
            }
            return new VerifiedData(consumer.getJwsHeaders().asMap(),
                                    consumer.getDecodedJwsPayload());
        } catch (JoseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JoseException("Signature Verification Failure");
        }
    }

    private Properties prepareSignatureProperties() {
        Properties props = new Properties();
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_TYPE, fraction.keystoreType());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_FILE, fraction.keystorePath());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_PSWD, fraction.keystorePassword());
        props.setProperty(JoseConstants.RSSEC_KEY_PSWD, fraction.keyPassword());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, fraction.keyAlias());
        props.setProperty(JoseConstants.RSSEC_SIGNATURE_ALGORITHM, fraction.signatureAlgorithm());
        return props;
    }

    @Override
    public String encrypt(String data) {
        return encrypt(Collections.emptyMap(), data);
    }

    @Override
    public String encrypt(Map<String, Object> metadata, String data) {
        JweHeaders headers = new JweHeaders();
        headers.asMap().putAll(metadata);
        Properties props = prepareEncryptionProperties();

        try {
            JweEncryptionProvider encryptor = JweUtils.loadEncryptionProvider(props, headers);
            JweCompactProducer producer = new JweCompactProducer(headers, data);
            return producer.encryptWith(encryptor);
        } catch (Exception ex) {
            throw new JoseException("Encryption Failure");
        }
    }

    @Override
    public String decrypt(String encryptedData) throws JoseException {
        return decryption(encryptedData).getData();
    }

    @Override
    public DecryptedData decryption(String encryptedData) throws JoseException {
        Properties props = prepareEncryptionProperties();

        try {
            JweCompactConsumer consumer = new JweCompactConsumer(encryptedData);
            JweDecryptionProvider decryptor = JweUtils.loadDecryptionProvider(props, consumer.getJweHeaders());
            String decryptedData = consumer.getDecryptedContentText(decryptor);
            return new DecryptedData(consumer.getJweHeaders().asMap(),
                                     decryptedData);
        } catch (Exception ex) {
            throw new JoseException("Decryption Failure");
        }
    }

    private Properties prepareEncryptionProperties() {
        Properties props = new Properties();
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_TYPE, fraction.keystoreType());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_FILE, fraction.keystorePath());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_PSWD, fraction.keystorePassword());
        props.setProperty(JoseConstants.RSSEC_KEY_PSWD, fraction.keyPassword());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, fraction.keyAlias());
        props.setProperty(JoseConstants.RSSEC_ENCRYPTION_KEY_ALGORITHM, fraction.keyEncryptionAlgorithm());
        props.setProperty(JoseConstants.RSSEC_ENCRYPTION_CONTENT_ALGORITHM, fraction.contentEncryptionAlgorithm());
        return props;
    }
}
