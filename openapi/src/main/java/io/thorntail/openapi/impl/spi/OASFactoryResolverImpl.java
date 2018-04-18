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

package io.thorntail.openapi.impl.spi;

import java.util.HashMap;
import java.util.Map;

import io.thorntail.openapi.impl.api.models.ComponentsImpl;
import io.thorntail.openapi.impl.api.models.ExternalDocumentationImpl;
import io.thorntail.openapi.impl.api.models.OperationImpl;
import io.thorntail.openapi.impl.api.models.PathItemImpl;
import io.thorntail.openapi.impl.api.models.PathsImpl;
import io.thorntail.openapi.impl.api.models.callbacks.CallbackImpl;
import io.thorntail.openapi.impl.api.models.headers.HeaderImpl;
import io.thorntail.openapi.impl.api.models.info.LicenseImpl;
import io.thorntail.openapi.impl.api.models.media.DiscriminatorImpl;
import io.thorntail.openapi.impl.api.models.media.EncodingImpl;
import io.thorntail.openapi.impl.api.models.media.MediaTypeImpl;
import io.thorntail.openapi.impl.api.models.media.SchemaImpl;
import io.thorntail.openapi.impl.api.models.media.XMLImpl;
import io.thorntail.openapi.impl.api.models.parameters.RequestBodyImpl;
import io.thorntail.openapi.impl.api.models.security.OAuthFlowsImpl;
import io.thorntail.openapi.impl.api.models.security.SecurityRequirementImpl;
import io.thorntail.openapi.impl.api.models.security.SecuritySchemeImpl;
import io.thorntail.openapi.impl.api.models.servers.ServerImpl;
import io.thorntail.openapi.impl.api.models.servers.ServerVariableImpl;
import io.thorntail.openapi.impl.api.models.tags.TagImpl;
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
import org.eclipse.microprofile.openapi.spi.OASFactoryResolver;
import io.thorntail.openapi.impl.api.models.OpenAPIImpl;
import io.thorntail.openapi.impl.api.models.examples.ExampleImpl;
import io.thorntail.openapi.impl.api.models.info.ContactImpl;
import io.thorntail.openapi.impl.api.models.info.InfoImpl;
import io.thorntail.openapi.impl.api.models.links.LinkImpl;
import io.thorntail.openapi.impl.api.models.media.ContentImpl;
import io.thorntail.openapi.impl.api.models.parameters.ParameterImpl;
import io.thorntail.openapi.impl.api.models.responses.APIResponseImpl;
import io.thorntail.openapi.impl.api.models.responses.APIResponsesImpl;
import io.thorntail.openapi.impl.api.models.security.OAuthFlowImpl;
import io.thorntail.openapi.impl.api.models.security.ScopesImpl;
import io.thorntail.openapi.impl.api.models.servers.ServerVariablesImpl;

/**
 * An implementation of the OpenAPI 1.0 spec's {@link OASFactoryResolver}.  This class
 * is responsible for constructing vendor specific models given a {@link Constructible}
 * model interface.
 *
 * @author eric.wittmann@gmail.com
 */
public class OASFactoryResolverImpl extends OASFactoryResolver {

    private static final Map<Class<? extends Constructible>, Class<? extends Constructible>> registry = new HashMap<>();

    static {
        registry.put(APIResponse.class, APIResponseImpl.class);
        registry.put(APIResponses.class, APIResponsesImpl.class);
        registry.put(Callback.class, CallbackImpl.class);
        registry.put(Components.class, ComponentsImpl.class);
        registry.put(Contact.class, ContactImpl.class);
        registry.put(Content.class, ContentImpl.class);
        registry.put(Discriminator.class, DiscriminatorImpl.class);
        registry.put(Encoding.class, EncodingImpl.class);
        registry.put(Example.class, ExampleImpl.class);
        registry.put(ExternalDocumentation.class, ExternalDocumentationImpl.class);
        registry.put(Header.class, HeaderImpl.class);
        registry.put(Info.class, InfoImpl.class);
        registry.put(License.class, LicenseImpl.class);
        registry.put(Link.class, LinkImpl.class);
        registry.put(MediaType.class, MediaTypeImpl.class);
        registry.put(OAuthFlow.class, OAuthFlowImpl.class);
        registry.put(OAuthFlows.class, OAuthFlowsImpl.class);
        registry.put(OpenAPI.class, OpenAPIImpl.class);
        registry.put(Operation.class, OperationImpl.class);
        registry.put(Parameter.class, ParameterImpl.class);
        registry.put(PathItem.class, PathItemImpl.class);
        registry.put(Paths.class, PathsImpl.class);
        registry.put(RequestBody.class, RequestBodyImpl.class);
        registry.put(Schema.class, SchemaImpl.class);
        registry.put(Scopes.class, ScopesImpl.class);
        registry.put(SecurityRequirement.class, SecurityRequirementImpl.class);
        registry.put(SecurityScheme.class, SecuritySchemeImpl.class);
        registry.put(Server.class, ServerImpl.class);
        registry.put(ServerVariable.class, ServerVariableImpl.class);
        registry.put(ServerVariables.class, ServerVariablesImpl.class);
        registry.put(Tag.class, TagImpl.class);
        registry.put(XML.class, XMLImpl.class);
    }

    /**
     * @see OASFactoryResolver#createObject(Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Constructible> T createObject(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        try {
            Class<? extends Constructible> implClass = registry.get(clazz);
            if (implClass == null) {
                throw new IllegalArgumentException("Class '" + clazz.getName() + "' is not Constructible.");
            }
            return (T) implClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
