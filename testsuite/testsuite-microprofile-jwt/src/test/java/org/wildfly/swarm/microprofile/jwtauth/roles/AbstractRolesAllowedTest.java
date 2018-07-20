/*
 *   Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.jwtauth.roles;

import java.io.InputStream;
import java.security.PrivateKey;

import io.smallrye.jwt.KeyUtils;
import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;

@RunWith(Arquillian.class)
public abstract class AbstractRolesAllowedTest {

    protected static WARArchive initDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.addClass(RolesEndpointClassLevel.class);
        deployment.addClass(TestApplication.class);
        deployment.addAsManifestResource(new ClassLoaderAsset("keys/public-key.pem"), "/MP-JWT-SIGNER");
        return deployment;
    }
    
    @RunAsClient
    @Test
    public void testRolesAllowed() throws Exception {
        String response = Request.Get("http://localhost:8080/mpjwt/rolesClass")
            .setHeader("Authorization", "Bearer " + createToken("Echoer"))
            .execute().returnContent().asString();
        Assert.assertEquals(response, "Hello jdoe@example.com");
    }
    
    @RunAsClient
    @Test
    public void testRolesNotAllowed() throws Exception {
        Assert.assertEquals(403, 
            Request.Get("http://localhost:8080/mpjwt/rolesClass")
                .setHeader("Authorization", "Bearer " + createToken("Echoer2"))
                .execute().returnResponse().getStatusLine().getStatusCode()); 
    }
    
    private static String createToken(String groupName) throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("http://testsuite-jwt-issuer.io");
        claims.setSubject("24400320");
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
        InputStream is = AbstractRolesAllowedTest.class.getResourceAsStream("/keys/private-key.pem");
        return KeyUtils.decodePrivateKey(new String(IOUtil.asByteArray(is)));
    }
    

}
