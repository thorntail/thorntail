/**
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
package org.eclipse.microprofile.jwt.wfswarm.tck;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.jwt.tck.util.ITokenParser;
import org.wildfly.swarm.microprofile.jwtauth.deployment.principal.DefaultJWTCallerPrincipalFactory;
import org.wildfly.swarm.microprofile.jwtauth.deployment.principal.JWTAuthContextInfo;
import org.wildfly.swarm.microprofile.jwtauth.deployment.principal.JWTCallerPrincipalFactory;

/**
 * MP-JWT TCK harness class to parse a token string
 */
public class TCKTokenParser implements ITokenParser {


    @Override
    public JsonWebToken parse(String bearerToken, String issuer, PublicKey publicKey) throws Exception {
        JWTAuthContextInfo authContextInfo = new JWTAuthContextInfo((RSAPublicKey) publicKey, issuer);
        JWTCallerPrincipalFactory factory = DefaultJWTCallerPrincipalFactory.instance();
        return factory.parse(bearerToken, authContextInfo);
    }

}
