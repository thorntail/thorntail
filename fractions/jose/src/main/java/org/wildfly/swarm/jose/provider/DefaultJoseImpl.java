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
package org.wildfly.swarm.jose.provider;

import org.apache.cxf.common.util.Base64UrlUtility;
import org.apache.cxf.common.util.StringUtils;
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
import org.wildfly.swarm.jose.DecryptionOutput;
import org.wildfly.swarm.jose.EncryptionInput;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseConfiguration;
import org.wildfly.swarm.jose.JoseException;
import org.wildfly.swarm.jose.JoseOperation;
import org.wildfly.swarm.jose.SignatureInput;
import org.wildfly.swarm.jose.VerificationOutput;

import java.util.List;
import java.util.Properties;

import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_JOSE_FORMAT;

public class DefaultJoseImpl implements Jose {
    private JoseConfiguration config;

    public DefaultJoseImpl(JoseConfiguration config) {
        this.config = config;
    }

    @Override
    public String sign(String data) {
        return sign(new SignatureInput(data));
    }

    @Override
    public String sign(SignatureInput input) {
        JwsHeaders headers = new JwsHeaders();
        headers.asMap().putAll(input.getHeaders());
        if (!config.signatureDataEncoding()) {
            headers.setPayloadEncodingStatus(false);
        }
        if (config.includeSignatureKeyAlias()) {
            headers.setKeyId(signatureKeyAlias());
        }
        Properties props = prepareSignatureVerificationProperties(JoseOperation.SIGN);
        headers.setSignatureAlgorithm(SignatureAlgorithm.getAlgorithm(config.signatureAlgorithm()));
        JwsSignatureProvider provider =
                JwsUtils.loadSignatureProvider(props, headers);

        return DEFAULT_JOSE_FORMAT == config.signatureFormat()
                ? signCompact(provider, headers, input.getData()) : signJson(provider, headers, input.getData());
    }

    private String signCompact(JwsSignatureProvider provider, JwsHeaders headers, String data) {
        try {
            JwsCompactProducer producer = new JwsCompactProducer(headers, data, config.signatureDataDetached());
            return producer.signWith(provider);
        } catch (Exception ex) {
            throw new JoseException("JWS Compact Signature Creation Failure", ex);
        }
    }

    private String signJson(JwsSignatureProvider provider, JwsHeaders headers, String data) {
        try {
            JwsJsonProducer producer = new JwsJsonProducer(data, true, config.signatureDataDetached());
            return producer.signWith(provider, headers);
        } catch (Exception ex) {
            throw new JoseException("JWS JOSE Signature Creation Failure", ex);
        }
    }

    @Override
    public String verify(String jws) throws JoseException {
        return verification(jws).getData();
    }

    @Override
    public VerificationOutput verification(String jws) throws JoseException {
        return getVerificationOutput(jws, null);
    }

    @Override
    public String verifyDetached(String jws, String detachedData) throws JoseException {
        return verificationDetached(jws, detachedData).getData();
    }

    @Override
    public VerificationOutput verificationDetached(String jws, String detachedData) throws JoseException {
        if (config.signatureDataEncoding()) {
            detachedData = Base64UrlUtility.encode(detachedData);
        }
        return getVerificationOutput(jws, detachedData);
    }

    private VerificationOutput getVerificationOutput(String jws, String detachedData) throws JoseException {
        Properties props = prepareSignatureVerificationProperties(JoseOperation.VERIFICATION);
        return DEFAULT_JOSE_FORMAT == config.signatureFormat()
                ? verifyCompact(props, jws, detachedData) : verifyJson(props, jws, detachedData);
    }

    private VerificationOutput verifyCompact(Properties props, String jws, String detachedData) {
        try {
            JwsCompactConsumer consumer = new JwsCompactConsumer(jws, detachedData);

            if (config.acceptSignatureAlias()) {
                JwsHeaders header = consumer.getJwsHeaders();
                props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, header.getKeyId());
            }

            JwsSignatureVerifier verifier =
                    JwsUtils.loadSignatureVerifier(props, consumer.getJwsHeaders());
            if (!consumer.verifySignatureWith(verifier)) {
                throw new JoseException("JWS Compact Signature Verification Failure");
            }
            return new VerificationOutput(consumer.getJwsHeaders().asMap(),
                    consumer.getDecodedJwsPayload());
        } catch (JoseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JoseException("JWS Compact Signature Verification Failure", ex);
        }
    }

    private VerificationOutput verifyJson(Properties props, String jws, String detachedData) {
        try {
            JwsJsonConsumer consumer = new JwsJsonConsumer(jws, detachedData);
            List<JwsJsonSignatureEntry> entries = consumer.getSignatureEntries();
            if (entries.size() > 1) {
                throw new JoseException("JWS JSON Signature Verification Failure:"
                        + " only a single recipient is supported at the moment");
            }
            JwsJsonSignatureEntry entry = entries.get(0);

            if (config.acceptSignatureAlias()) {
                JwsHeaders header = entry.getProtectedHeader();
                props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, header.getKeyId());
            }

            JwsSignatureVerifier verifier = JwsUtils.loadSignatureVerifier(props, entry.getProtectedHeader());
            if (!entry.verifySignatureWith(verifier)) {
                throw new JoseException("JWS JSON Signature Verification Failure");
            }
            return new VerificationOutput(entry.getProtectedHeader().asMap(),
                    consumer.getDecodedJwsPayload());
        } catch (JoseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JoseException("JWS JSON Signature Verification Failure", ex);
        }
    }

    @Override
    public String encrypt(String data) {
        return encrypt(new EncryptionInput(data));
    }

    @Override
    public String encrypt(EncryptionInput input) {
        Properties props = prepareEncryptionDecryptionProperties(JoseOperation.ENCRYPTION);
        JweHeaders headers = new JweHeaders();
        headers.asMap().putAll(input.getHeaders());
        if (config.includeEncryptionKeyAlias()) {
            headers.setKeyId(encryptionKeyAlias());
        }
        JweEncryptionProvider provider = JweUtils.loadEncryptionProvider(props, headers);

        return DEFAULT_JOSE_FORMAT == config.encryptionFormat()
                ? encryptCompact(provider, headers, input.getData()) : encryptJson(provider, headers, input.getData());
    }


    private String encryptCompact(JweEncryptionProvider provider, JweHeaders headers, String data) {
        try {
            JweCompactProducer producer = new JweCompactProducer(headers, data);
            return producer.encryptWith(provider);
        } catch (Exception ex) {
            throw new JoseException("JWE Compact Encryption Failure", ex);
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
    public String decrypt(String jwe) throws JoseException {
        return decryption(jwe).getData();
    }

    @Override
    public DecryptionOutput decryption(String jwe) throws JoseException {
        Properties props = prepareEncryptionDecryptionProperties(JoseOperation.DECRYPTION);

        return DEFAULT_JOSE_FORMAT == config.signatureFormat()
                ? decryptCompact(props, jwe) : decryptJson(props, jwe);
    }

    private DecryptionOutput decryptCompact(Properties props, String jwe) {
        try {
            JweCompactConsumer consumer = new JweCompactConsumer(jwe);

            if (config.acceptDecryptionAlias()) {
                JweHeaders header = consumer.getJweHeaders();
                props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, header.getKeyId());
            }

            JweDecryptionProvider decryptor = JweUtils.loadDecryptionProvider(props, consumer.getJweHeaders());
            String decryptedData = consumer.getDecryptedContentText(decryptor);
            return new DecryptionOutput(consumer.getJweHeaders().asMap(),
                    decryptedData);
        } catch (Exception ex) {
            throw new JoseException("JWE Compact Decryption Failure");
        }
    }

    private DecryptionOutput decryptJson(Properties props, String jwe) {
        try {
            JweJsonConsumer consumer = new JweJsonConsumer(jwe);

            if (consumer.getRecipients().size() > 1) {
                throw new JoseException("JWE JSON Decryption Failure:"
                        + " only a single recipient is supported at the moment");
            }

            if (config.acceptDecryptionAlias()) {
                JweHeaders header = consumer.getProtectedHeader();
                props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, header.getKeyId());
            }

            JweDecryptionProvider decryptor = JweUtils.loadDecryptionProvider(props, consumer.getProtectedHeader());
            JweDecryptionOutput output = consumer.decryptWith(decryptor);
            return new DecryptionOutput(consumer.getProtectedHeader().asMap(),
                    output.getContentText());
        } catch (JoseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JoseException("JWE JSON Decryption Failure", ex);
        }
    }

    private Properties prepareEncryptionDecryptionProperties(JoseOperation operation) {
        Properties props = new Properties();

        props.setProperty(JoseConstants.RSSEC_KEY_STORE_TYPE, config.keystoreType());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_FILE, config.keystorePath());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_PSWD, config.keystorePassword());
        props.setProperty(JoseConstants.RSSEC_KEY_PSWD, config.encryptionKeyPassword());
        props.setProperty(JoseConstants.RSSEC_ENCRYPTION_KEY_ALGORITHM, config.keyEncryptionAlgorithm());
        props.setProperty(JoseConstants.RSSEC_ENCRYPTION_CONTENT_ALGORITHM, config.contentEncryptionAlgorithm());

        if (operation.equals(JoseOperation.ENCRYPTION)) {
            props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, encryptionKeyAlias());

        } else if (operation.equals(JoseOperation.DECRYPTION)) {
            props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, decryptionKeyAlias());

        }
        return props;
    }

    private Properties prepareSignatureVerificationProperties(JoseOperation operation) {
        Properties props = new Properties();
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_TYPE, config.keystoreType());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_FILE, config.keystorePath());
        props.setProperty(JoseConstants.RSSEC_KEY_STORE_PSWD, config.keystorePassword());
        props.setProperty(JoseConstants.RSSEC_KEY_PSWD, config.signatureKeyPassword());
        props.setProperty(JoseConstants.RSSEC_SIGNATURE_ALGORITHM, config.signatureAlgorithm());

        if (operation.equals(JoseOperation.SIGN)) {
            props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, signatureKeyAlias());

        } else if (operation.equals(JoseOperation.VERIFICATION)) {
            props.setProperty(JoseConstants.RSSEC_KEY_STORE_ALIAS, verificationKeyAlias());
        }
        return props;
    }

    private String signatureKeyAlias() {
        if (config.signatureKeyAliasOut() == null) {
            return config.signatureKeyAlias();
        }
        return config.signatureKeyAliasOut();
    }

    private String verificationKeyAlias() {
        if (config.signatureKeyAliasIn() == null) {
            return config.signatureKeyAlias();
        }
        return config.signatureKeyAliasIn();
    }

    private String encryptionKeyAlias() {
        if (config.encryptionKeyAliasOut() == null) {
            return config.encryptionKeyAlias();
        }
        return config.encryptionKeyAliasOut();
    }

    private String decryptionKeyAlias() {
        if (config.encryptionKeyAliasIn() == null) {
            return config.encryptionKeyAlias();
        }
        return config.encryptionKeyAliasIn();
    }

}