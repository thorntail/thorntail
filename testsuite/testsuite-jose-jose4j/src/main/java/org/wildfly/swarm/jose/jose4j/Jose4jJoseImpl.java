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

import org.wildfly.swarm.jose.DecryptionOutput;
import org.wildfly.swarm.jose.EncryptionInput;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseException;
import org.wildfly.swarm.jose.JoseFraction;
import org.wildfly.swarm.jose.SignatureInput;
import org.wildfly.swarm.jose.VerificationOutput;

public class Jose4jJoseImpl implements Jose {
    private JoseFraction fraction;
    public Jose4jJoseImpl(JoseFraction fraction) {
       if (DEFAULT_JOSE_FORMAT != fraction.signatureFormat()
           || DEFAULT_JOSE_FORMAT != fraction.encryptionFormat()) {
            throw new IllegalStateException("JWS and JWE JSON formats are not supported");
        }
       this.fraction = fraction;
    }

    @Override
    public String sign(String data) {
        return sign(new SignatureInput(data));
    }

    @Override
    public String sign(SignatureInput input) {
        return "jose4j+jws";
    }

    @Override
    public String verify(String jws) throws JoseException {
        return verification(jws).getData();
    }

    @Override
    public VerificationOutput verification(String jws) throws JoseException {
        return new VerificationOutput("jose4j+jws");
    }

    @Override
    public String verifyDetached(String jws, String detachedData) throws JoseException {
        return verificationDetached(jws, detachedData).getData();
    }

    @Override
    public VerificationOutput verificationDetached(String jws, String detachedData) throws JoseException {
        return null;
    }


    @Override
    public String encrypt(String data) {
        return encrypt(new EncryptionInput(data));
    }

    @Override
    public String encrypt(EncryptionInput input) {
        return "jose4j+jwe";
    }

    @Override
    public String decrypt(String jwe) throws JoseException {
        return decryption(jwe).getData();
    }

    @Override
    public DecryptionOutput decryption(String jwe) throws JoseException {
        return new DecryptionOutput("jose4j+jwe");
    }

}
