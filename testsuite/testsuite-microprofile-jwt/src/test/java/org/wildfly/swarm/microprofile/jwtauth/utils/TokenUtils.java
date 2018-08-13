/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.jwtauth.utils;

import io.smallrye.jwt.KeyUtils;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.wildfly.swarm.microprofile.jwtauth.ContentTypesTest;

import java.io.InputStream;
import java.security.PrivateKey;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 7/9/18
 */
public class TokenUtils {

    private TokenUtils() {
    }

    public static final String SUBJECT = "24400320";

    public static String createToken(String groupName) throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("http://testsuite-jwt-issuer.io");
        claims.setSubject(SUBJECT);
        claims.setStringListClaim("groups", groupName);
        claims.setClaim("upn", "jdoe@example.com");
        claims.setExpirationTimeMinutesInTheFuture(1);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setKey(getPrivateKey());
        return jws.getCompactSerialization();
    }

    private static PrivateKey getPrivateKey() throws Exception {
        InputStream is = ContentTypesTest.class.getResourceAsStream("/keys/private-key.pem");
        return KeyUtils.decodePrivateKey(new String(IOUtil.asByteArray(is)));
    }
}
