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
package org.wildfly.swarm.jose.jose4j;

import static org.wildfly.swarm.jose.JoseProperties.DEFAULT_JOSE_FORMAT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.Map;
import java.util.stream.Collectors;

import org.jose4j.base64url.Base64Url;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.wildfly.swarm.jose.DecryptionOutput;
import org.wildfly.swarm.jose.EncryptionInput;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseConfiguration;
import org.wildfly.swarm.jose.JoseException;
import org.wildfly.swarm.jose.SignatureInput;
import org.wildfly.swarm.jose.VerificationOutput;

public class Jose4jJoseImpl implements Jose {
    private JoseConfiguration config;
    public Jose4jJoseImpl(JoseConfiguration config) {
        this.config = config;
        if (DEFAULT_JOSE_FORMAT != this.config.signatureFormat()
           || DEFAULT_JOSE_FORMAT != this.config.encryptionFormat()) {
            throw new IllegalStateException("JWS and JWE JSON formats are not supported");
       }
       if (!"jwk".equals(this.config.keystoreType())) {
           throw new IllegalStateException("JKS keystore type is not supported");
       }
    }

    @Override
    public String sign(String data) {
        return sign(new SignatureInput(data));
    }

    @Override
    public String sign(SignatureInput input) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(input.getData());
        for (Map.Entry<String, Object> entry : input.getMetadata().entrySet()) {
            jws.getHeaders().setObjectHeaderValue(entry.getKey(), entry.getValue());
        }
        jws.setAlgorithmHeaderValue(config.signatureAlgorithm());
        if (!config.signatureDataEncoding()) {
            jws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, false);
            jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);
        }
        jws.setKey(getSignatureKey());
        try {
            return config.signatureDataDetached()
                ? jws.getDetachedContentCompactSerialization() : jws.getCompactSerialization();
        } catch (org.jose4j.lang.JoseException ex) {
            throw new JoseException(ex.getMessage(), ex);
        }
    }

    @Override
    public String verify(String jws) throws JoseException {
        return verification(jws).getData();
    }

    @Override
    public VerificationOutput verification(String compactJws) throws JoseException {
        return getVerificationOutput(compactJws, null);
    }

    @Override
    public String verifyDetached(String compactJws, String detachedData) throws JoseException {
        return verificationDetached(compactJws, detachedData).getData();
    }

    @Override
    public VerificationOutput verificationDetached(String compactJws, String detachedData) throws JoseException {
        return getVerificationOutput(compactJws, detachedData);
    }


    public VerificationOutput getVerificationOutput(String compactJws, String detached) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, config.signatureAlgorithm()));
        jws.setKey(getSignatureKey());
        try {
            jws.setCompactSerialization(compactJws);
            if (detached != null) {
                if (config.signatureDataEncoding()) {
                    jws.setEncodedPayload(new Base64Url().base64UrlEncodeUtf8ByteRepresentation(detached));
                } else {
                    jws.setPayload(detached);
                }
            }
        } catch (org.jose4j.lang.JoseException ex) {
            throw new JoseException(ex.getMessage(), ex);
        }
        try {
            String jwsPayload = jws.getPayload();
            return new VerificationOutput(jwsPayload);
        } catch (org.jose4j.lang.JoseException ex) {
            throw new JoseException(ex.getMessage(), ex);
        }
    }

    @Override
    public String encrypt(String data) {
        return encrypt(new EncryptionInput(data));
    }

    @Override
    public String encrypt(EncryptionInput input) {
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setPlaintext(input.getData());
        for (Map.Entry<String, Object> entry : input.getMetadata().entrySet()) {
            jwe.getHeaders().setObjectHeaderValue(entry.getKey(), entry.getValue());
        }
        jwe.setAlgorithmHeaderValue(config.keyEncryptionAlgorithm());
        jwe.setEncryptionMethodHeaderParameter(config.contentEncryptionAlgorithm());
        jwe.setKey(getEncryptionKey());
        try {
            return jwe.getCompactSerialization();
        } catch (org.jose4j.lang.JoseException ex) {
            throw new JoseException(ex.getMessage(), ex);
        }
    }

    @Override
    public String decrypt(String jwe) throws JoseException {
        return decryption(jwe).getData();
    }

    @Override
    public DecryptionOutput decryption(String compactJwe) throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        try {
            jwe.setCompactSerialization(compactJwe);
        } catch (org.jose4j.lang.JoseException ex) {
            throw new JoseException(ex.getMessage(), ex);
        }
        jwe.setAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, config.keyEncryptionAlgorithm()));
        jwe.setContentEncryptionAlgorithmConstraints(
            new AlgorithmConstraints(ConstraintType.WHITELIST, config.contentEncryptionAlgorithm()));
        jwe.setKey(getEncryptionKey());
        try {
            return new DecryptionOutput(jwe.getPlaintextString());
        } catch (org.jose4j.lang.JoseException ex) {
            throw new JoseException(ex.getMessage(), ex);
        }
    }

    private Key getSignatureKey() {
        return getJwkKey(config.signatureKeyAlias(), config.signatureAlgorithm());
    }
    private Key getEncryptionKey() {
        return getJwkKey(config.encryptionKeyAlias(), config.contentEncryptionAlgorithm());
    }

    private Key getJwkKey(String kid, String keyAlgorithm) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String jwkSetJson = null;
        try (BufferedReader is = new BufferedReader(new InputStreamReader(cl.getResourceAsStream(config.keystorePath())))) {
            jwkSetJson = is.lines().collect(Collectors.joining("\n"));
        }  catch (IOException ex) {
            throw new JoseException("Keystore can not be loaded", ex);
        }
        JsonWebKeySet jwkSet = null;
        try {
            jwkSet = new JsonWebKeySet(jwkSetJson);
        } catch (org.jose4j.lang.JoseException ex) {
            throw new JoseException(ex.getMessage(), ex);
        }
        JsonWebKey jwk = jwkSet.findJsonWebKey(kid, null, null, keyAlgorithm);
        if (jwk != null) {
            return jwk.getKey();
        } else {
            throw new JoseException("Key is not available");
        }
    }
}
