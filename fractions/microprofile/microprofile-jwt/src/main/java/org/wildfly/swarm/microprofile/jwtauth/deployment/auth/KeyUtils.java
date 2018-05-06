/*
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.wildfly.swarm.microprofile.jwtauth.deployment.auth;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class KeyUtils {
    private static final String RSA = "RSA";

    public static PrivateKey readPrivateKey(String pemResName) throws Exception {
        InputStream contentIS = KeyUtils.class.getResourceAsStream(pemResName);
        byte[] tmp = new byte[4096];
        int length = contentIS.read(tmp);
        PrivateKey privateKey = decodePrivateKey(new String(tmp, 0, length));
        return privateKey;
    }

    public static PublicKey readPublicKey(String pemResName) throws Exception {
        InputStream contentIS = KeyUtils.class.getResourceAsStream(pemResName);
        byte[] tmp = new byte[4096];
        int length = contentIS.read(tmp);
        PublicKey publicKey = decodePublicKey(new String(tmp, 0, length));
        return publicKey;
    }

    public static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair;
    }

    public static PrivateKey decodePrivateKey(String pemEncoded) throws Exception {
        pemEncoded = removeBeginEnd(pemEncoded);
        byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(pemEncoded);

        // extract the private key

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        PrivateKey privKey = kf.generatePrivate(keySpec);
        return privKey;
    }

    /**
     * Decode a JWK(S) encoded public key string to an RSA PublicKey. This assumes a single JWK in the set as
     * only the first JWK is used.
     * @param jwksValue - JWKS string value
     * @return PublicKey from RSAPublicKeySpec
     */
    public static PublicKey decodeJWKSPublicKey(String jwksValue) throws Exception {
        JsonObject jwks;
        try {
            jwks = Json.createReader(new StringReader(jwksValue)).readObject();
        } catch (Exception e) {
            // See if this is base64 encoded
            byte[] decoded = Base64.getDecoder().decode(jwksValue);
            jwks = Json.createReader(new ByteArrayInputStream(decoded)).readObject();
        }
        JsonArray keys = jwks.getJsonArray("keys");
        JsonObject jwk;
        if (keys != null) {
            jwk = keys.getJsonObject(0);
        } else {
            // A JWK
            jwk = jwks;
        }
        String e = jwk.getString("e");
        String n = jwk.getString("n");

        byte[] ebytes = Base64.getUrlDecoder().decode(e);
        BigInteger publicExponent = new BigInteger(1, ebytes);
        byte[] nbytes = Base64.getUrlDecoder().decode(n);
        BigInteger modulus = new BigInteger(1, nbytes);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        PublicKey publicKey = kf.generatePublic(rsaPublicKeySpec);
        return publicKey;
    }

    /**
     * Decode a PEM encoded public key string to an RSA PublicKey
     * @param pemEncoded - PEM string for private key
     * @return PublicKey
     * @throws Exception on decode failure
     */
    public static PublicKey decodePublicKey(String pemEncoded) throws Exception {
        pemEncoded = removeBeginEnd(pemEncoded);
        byte[] encodedBytes = Base64.getDecoder().decode(pemEncoded);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedBytes);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        return kf.generatePublic(spec);
    }

    /**
     * Strip any -----BEGIN*KEY... header and -----END*KEY... footer and newlines
     * @param pem encoded string with option header/footer
     * @return a single base64 encoded pem string
     */
    private static String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN(.*)KEY-----", "");
        pem = pem.replaceAll("-----END(.*)KEY-----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        return pem.trim();
    }
    private KeyUtils() {
    }
}
