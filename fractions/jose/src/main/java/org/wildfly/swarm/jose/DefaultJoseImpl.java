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

import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_JOSE_FORMAT;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.rs.security.jose.common.JoseConstants;
import org.apache.cxf.rs.security.jose.jwa.SignatureAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.JweCompactConsumer;
import org.apache.cxf.rs.security.jose.jwe.JweCompactProducer;
import org.apache.cxf.rs.security.jose.jwe.JweDecryptionOutput;
import org.apache.cxf.rs.security.jose.jwe.JweDecryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.JweEncryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.JweHeaders;
import org.apache.cxf.rs.security.jose.jwe.JweJsonConsumer;
import org.apache.cxf.rs.security.jose.jwe.JweJsonProducer;
import org.apache.cxf.rs.security.jose.jwe.JweUtils;
import org.apache.cxf.rs.security.jose.jws.JwsCompactConsumer;
import org.apache.cxf.rs.security.jose.jws.JwsCompactProducer;
import org.apache.cxf.rs.security.jose.jws.JwsHeaders;
import org.apache.cxf.rs.security.jose.jws.JwsJsonConsumer;
import org.apache.cxf.rs.security.jose.jws.JwsJsonProducer;
import org.apache.cxf.rs.security.jose.jws.JwsJsonSignatureEntry;
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
        if (!fraction.signatureDataEncoding()) {
            headers.setPayloadEncodingStatus(false);
        }
        Properties props = prepareSignatureProperties();
        headers.setSignatureAlgorithm(SignatureAlgorithm.getAlgorithm(fraction.signatureAlgorithm()));
        JwsSignatureProvider provider =
            JwsUtils.loadSignatureProvider(prepareMessage(), props, headers);

        return DEFAULT_JOSE_FORMAT == fraction.signatureFormat()
            ? signCompact(provider, headers, data) : signJson(provider, headers, data);
    }

    private String signCompact(JwsSignatureProvider provider, JwsHeaders headers, String data) {
        try {
            JwsCompactProducer producer = new JwsCompactProducer(headers, data);
            return producer.signWith(provider);
        } catch (Exception ex) {
            throw new JoseException("JWS Compact Signature Creation Failure", ex);
        }
    }

    private String signJson(JwsSignatureProvider provider, JwsHeaders headers, String data) {
        try {
            JwsJsonProducer producer = new JwsJsonProducer(data, true);
            return producer.signWith(provider, headers);
        } catch (Exception ex) {
            throw new JoseException("JWS JOSE Signature Creation Failure", ex);
        }
    }

    @Override
    public String verify(String signedData) throws JoseException {
        return verification(signedData).getData();
    }

    @Override
    public VerifiedData verification(String signedData) throws JoseException {
        Properties props = prepareSignatureProperties();
        return DEFAULT_JOSE_FORMAT == fraction.signatureFormat()
            ? verifyCompact(props, signedData) : verifyJson(props, signedData);
    }

    private VerifiedData verifyCompact(Properties props, String signedData) {
        try {
            JwsCompactConsumer consumer = new JwsCompactConsumer(signedData);
            JwsSignatureVerifier verifier =
                JwsUtils.loadSignatureVerifier(prepareMessage(), props, consumer.getJwsHeaders());
            if (!consumer.verifySignatureWith(verifier)) {
                throw new JoseException("JWS Compact Signature Verification Failure");
            }
            return new VerifiedData(consumer.getJwsHeaders().asMap(),
                                    consumer.getDecodedJwsPayload());
        } catch (JoseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JoseException("JWS Compact Signature Verification Failure", ex);
        }
    }

    private VerifiedData verifyJson(Properties props, String signedData) {
        try {
            JwsJsonConsumer consumer = new JwsJsonConsumer(signedData);
            List<JwsJsonSignatureEntry> entries = consumer.getSignatureEntries();
            if (entries.size() > 1) {
                throw new JoseException("JWS JSON Signature Verification Failure:"
                    + " only a single recipient is supported at the moment");
            }
            JwsJsonSignatureEntry entry = entries.get(0);
            JwsSignatureVerifier verifier = JwsUtils.loadSignatureVerifier(props, entry.getProtectedHeader());
            if (!entry.verifySignatureWith(verifier)) {
                throw new JoseException("JWS JSON Signature Verification Failure");
            }
            return new VerifiedData(entry.getProtectedHeader().asMap(),
                                    consumer.getDecodedJwsPayload());
        } catch (JoseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JoseException("JWS JSON Signature Verification Failure", ex);
        }
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
        JweEncryptionProvider provider = JweUtils.loadEncryptionProvider(props, headers);

        return DEFAULT_JOSE_FORMAT == fraction.encryptionFormat()
                ? encryptCompact(provider, headers, data) : encryptJson(provider, headers, data);
    }

    private String encryptCompact(JweEncryptionProvider provider, JweHeaders headers, String data) {
        try {
            JweCompactProducer producer = new JweCompactProducer(headers, data);
            return producer.encryptWith(provider);
        } catch (Exception ex) {
            throw new JoseException("JWE Compact Encryption Failure");
        }
    }

    private String encryptJson(JweEncryptionProvider provider, JweHeaders headers, String data) {
        try {
            JweJsonProducer producer = new JweJsonProducer(headers, StringUtils.toBytesUTF8(data), true);
            return producer.encryptWith(provider);
        } catch (Exception ex) {
            throw new JoseException("JWE JSON Encryption Failure", ex);
        }
    }

    @Override
    public String decrypt(String encryptedData) throws JoseException {
        return decryption(encryptedData).getData();
    }

    @Override
    public DecryptedData decryption(String encryptedData) throws JoseException {
        Properties props = prepareEncryptionProperties();

        return DEFAULT_JOSE_FORMAT == fraction.signatureFormat()
                ? decryptCompact(props, encryptedData) : decryptJson(props, encryptedData);
    }

    private DecryptedData decryptCompact(Properties props, String encryptedData) {
        try {
            JweCompactConsumer consumer = new JweCompactConsumer(encryptedData);
            JweDecryptionProvider decryptor = JweUtils.loadDecryptionProvider(props, consumer.getJweHeaders());
            String decryptedData = consumer.getDecryptedContentText(decryptor);
            return new DecryptedData(consumer.getJweHeaders().asMap(),
                                     decryptedData);
        } catch (Exception ex) {
            throw new JoseException("JWE Compact Decryption Failure");
        }
    }

    private DecryptedData decryptJson(Properties props, String encryptedData) {
        try {
            JweJsonConsumer consumer = new JweJsonConsumer(encryptedData);
            if (consumer.getRecipients().size() > 1) {
                throw new JoseException("JWE JSON Decryption Failure:"
                    + " only a single recipient is supported at the moment");
            }
            JweDecryptionProvider decryptor = JweUtils.loadDecryptionProvider(props, consumer.getProtectedHeader());
            JweDecryptionOutput output = consumer.decryptWith(decryptor);
            return new DecryptedData(consumer.getProtectedHeader().asMap(),
                                     output.getContentText());
        } catch (JoseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JoseException("JWE JSON Decryption Failure", ex);
        }
    }

    private Properties prepareSignatureProperties() {
        Properties props = new Properties();
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_TYPE, fraction.keystoreType());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_FILE, fraction.keystorePath());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_PSWD, fraction.keystorePassword());
        props.setProperty(JoseConstants.RSSEC_KEY_PSWD, fraction.signatureKeyPassword());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, fraction.signatureKeyAlias());
        props.setProperty(JoseConstants.RSSEC_SIGNATURE_ALGORITHM, fraction.signatureAlgorithm());
        return props;
    }

    private Properties prepareEncryptionProperties() {
        Properties props = new Properties();
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_TYPE, fraction.keystoreType());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_FILE, fraction.keystorePath());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_PSWD, fraction.keystorePassword());
        props.setProperty(JoseConstants.RSSEC_KEY_PSWD, fraction.encryptionKeyPassword());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, fraction.encryptionKeyAlias());
        props.setProperty(JoseConstants.RSSEC_ENCRYPTION_KEY_ALGORITHM, fraction.keyEncryptionAlgorithm());
        props.setProperty(JoseConstants.RSSEC_ENCRYPTION_CONTENT_ALGORITHM, fraction.contentEncryptionAlgorithm());
        return props;
    }

    // TODO:
    // If the key store is JWK (set) then CXF JwkUtils will NPE while trying to load it
    // if the CXF Message is null; fix it to avoid passing an empty message
    private Message prepareMessage() {
        Message m = new MessageImpl();
        m.setExchange(new ExchangeImpl());
        return m;
    }
}
