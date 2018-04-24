package org.wildfly.swarm.microprofile.jwtauth.keys;

/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

import java.io.File;

import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.microprofile.jwtauth.roles.KeyLoadingService;
import org.wildfly.swarm.microprofile.jwtauth.roles.TestApplication;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
public class ClasspathResourceTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {

        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "pubkey.war");
        deployment.addResource(TestApplication.class);
        deployment.addResource(KeyLoadingService.class);
        deployment.addAsResource(new ClassLoaderAsset("keys/public-key.pem"), "/public-key.pem");
        deployment.addAsResource(new ClassLoaderAsset("cp-resource.yaml"), "/project-defaults.yml");

        return deployment;
    }

    @Test
    @RunAsClient
    public void verifyPubKeyLoading() throws Exception {
        String fileName = "./src/test/resources/keys/public-key.pem";
        File pubKeyFile = new File(fileName);
        
        String result = Request.Get("http://localhost:8080/mpjwt/signer-key").execute().returnContent().asString();
        Assert.assertEquals(pubKeyFile.length(), result.length());
    }
}
