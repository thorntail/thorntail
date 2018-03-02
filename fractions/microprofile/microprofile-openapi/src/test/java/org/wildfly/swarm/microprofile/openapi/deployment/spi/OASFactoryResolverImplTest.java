/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.openapi.deployment.spi;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.Scopes;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.junit.Assert;
import org.junit.Test;
import org.wildfly.swarm.microprofile.openapi.api.models.info.LicenseImpl;

/**
 * @author eric.wittmann@gmail.com
 */
public class OASFactoryResolverImplTest {

    /**
     * Test method for
     * {@link org.wildfly.swarm.microprofile.openapi.deployment.spi.OASFactoryResolverImpl#createObject(java.lang.Class)}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testCreateObject_All() {
        Class modelClasses[] = { APIResponse.class, APIResponses.class, Callback.class, Components.class,
                Contact.class, Content.class, Discriminator.class, Encoding.class, Example.class,
                ExternalDocumentation.class, Header.class, Info.class, License.class, Link.class, MediaType.class,
                OAuthFlow.class, OAuthFlows.class, OpenAPI.class, Operation.class, Parameter.class, PathItem.class,
                Paths.class, RequestBody.class, Schema.class, Scopes.class, SecurityRequirement.class,
                SecurityScheme.class, Server.class, ServerVariable.class, ServerVariables.class, Tag.class, XML.class };
        for (Class modelClass : modelClasses) {
            Constructible object = OASFactory.createObject(modelClass);
            Assert.assertNotNull(object);
        }
    }

    /**
     * Test method for
     * {@link org.wildfly.swarm.microprofile.openapi.deployment.spi.OASFactoryResolverImpl#createObject(java.lang.Class)}.
     */
    @Test
    public void testCreateObject_License() {
        License license = OASFactory.createObject(License.class).name("Test License").url("urn:test-url");
        Assert.assertNotNull(license);
        Assert.assertEquals(LicenseImpl.class, license.getClass());
        Assert.assertEquals("Test License", license.getName());
        Assert.assertEquals("urn:test-url", license.getUrl());
    }

    /**
     * Test method for
     * {@link org.wildfly.swarm.microprofile.openapi.deployment.spi.OASFactoryResolverImpl#createObject(java.lang.Class)}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testCreateObject_RTE() {
        Class c = String.class;
        try {
            OASFactory.createObject(c);
            Assert.fail("Expected a runtime error.");
        } catch (RuntimeException e) {
            Assert.assertEquals("Class 'java.lang.String' is not Constructible.", e.getMessage());
        }
    }

}
