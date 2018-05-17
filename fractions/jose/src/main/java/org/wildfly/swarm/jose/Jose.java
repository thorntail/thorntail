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

/**
 * Supports the protection of data with JOSE Signature (RFC7515) and Encryption (RFC7516)
 * using JSON Web Algorithms (RFC7518).
 */
public interface Jose {
    /**
     * Sign the data in the JWS Compact or JSON (optional) format.
     * @param data the data to be signed
     * @return the signed data in the JWS Compact or JSON format
     * @throws JoseException
     */
    String sign(String data) throws JoseException;

    /**
     * Sign the data in the JWS Compact or JSON (optional) format.
     * @param input the data and optional JWS headers which have to be integrity-protected
     * @return the signed data in the JWS Compact or JSON format
     * @throws JoseException
     */
    String sign(SignatureInput input) throws JoseException;

    /**
     * Verify the signed data in the JWS compact or JSON (optional) format.
     * @param jws the JWS sequence.
     * @return verified data
     * @throws JoseException
     */
    String verify(String jws) throws JoseException;

    /**
     * Verify the signed data in the JWS compact or JSON (optional) format.
     * @param jws the JWS sequence.
     * @return verified data and metadata
     * @throws JoseException
     */
    VerificationOutput verification(String jws) throws JoseException;

    /**
     * Verify the signed and detached data in the JWS compact or JSON (optional) format.
     * @param jws the JWS sequence.
     * @param detachedData the signed and detached data.
     * @return verified data
     * @throws JoseException
     */
    String verifyDetached(String jws, String detachedData) throws JoseException;

    /**
     * Verify the signed and detached data in the JWS compact or JSON (optional) format.
     * @param jws the JWS sequence.
     * @param detachedData the signed and detached data.
     * @return verified data and metadata
     * @throws JoseException
     */
    VerificationOutput verificationDetached(String jws, String detachedData) throws JoseException;

    /**
     * Encrypt the data in the JWE compact or JSON (optional) format..
     * @param data the data to be encrypted
     * @return the encrypted data
     * @throws JoseException
     */
    String encrypt(String data) throws JoseException;

    /**
     * Encrypt the data in the JWE compact or JSON (optional) format.
     * @param input the data and optional metadata which have to be encrypted and integrity-protected
     * @return the encrypted data
     * @throws JoseException
     */
    String encrypt(EncryptionInput input) throws JoseException;

    /**
     * Decrypt the encrypted data in the JWE compact or JSON (optional) format.
     * @param jwe the JWE sequence.
     * @return decrypted data
     * @throws JoseException
     */
    String decrypt(String jwe) throws JoseException;

    /**
     * Decrypt the encrypted data in the JWE compact or JSON (optional) format.
     * @param jwe the JWE sequence.
     * @return decrypted data and verified metadata
     * @throws JoseException
     */
    DecryptionOutput decryption(String jwe) throws JoseException;
}