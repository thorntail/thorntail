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

import java.util.Map;

/**
 * Supports the protection of data with JOSE Signature and Encryption algorithms
 */
public interface Jose {
    /**
     * Sign the data in the JOSE JWS Compact or JSON (optional) format.
     * @param data the data to be signed
     * @return the signed data in the JOSE JWS Compact format
     * @throws JoseException
     */
    String sign(String data) throws JoseException;

    /**
     * Sign the data in the JOSE JWS Compact or JSON (optional) format.
     * @param metadata the data to be integrity-protected as JOSE JWS headers
     * @param data the data to be signed
     * @return the signed data in the JOSE JWS Compact or JSON format
     * @throws JoseException
     */
    String sign(Map<String, Object> metadata, String data) throws JoseException;

    /**
     * Verify the signed data in the JOSE JWS compact or JSON (optional) format.
     * @param signedData the signed data.
     * @return verified data
     * @throws JoseException
     */
    String verify(String signedData) throws JoseException;

    /**
     * Verify the signed data in the JOSE JWS compact or JSON (optional) format.
     * @param signedData the signed data.
     * @return verified metadata and data
     * @throws JoseException
     */
    VerifiedData verification(String signedData) throws JoseException;

    /**
     * Encrypt the data in the JOSE JWE compact or JSON (optional) format..
     * @param data the data to be encrypted
     * @return the encrypted data
     * @throws JoseException
     */
    String encrypt(String data) throws JoseException;

    /**
     * Encrypt the data in the JOSE JWE compact or JSON (optional) format.
     * @param metadata the data to be integrity-protected as JOSE JWE headers
     * @param data the data to be encrypted
     * @return the encrypted data
     * @throws JoseException
     */
    String encrypt(Map<String, Object> metadata, String data) throws JoseException;

    /**
     * Decrypt the encrypted data in the JOSE JWE compact or JSON (optional) format.
     * @param encryptedData the encrypted data
     * @return decrypted data
     * @throws JoseException
     */
    String decrypt(String encryptedData) throws JoseException;

    /**
     * Decrypt the encrypted data in the JOSE JWE compact or JSON (optional) format.
     * @param encryptedData the encrypted data.
     * @return verified metadata and decrypted data
     * @throws JoseException
     */
    DecryptedData decryption(String encryptedData) throws JoseException;
}