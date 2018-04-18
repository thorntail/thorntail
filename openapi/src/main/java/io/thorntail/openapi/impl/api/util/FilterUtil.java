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

package io.thorntail.openapi.impl.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
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
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.Scopes;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;
import org.eclipse.microprofile.openapi.models.tags.Tag;

/**
 * @author eric.wittmann@gmail.com
 */
public class FilterUtil {

    /**
     * Constructor.
     */
    private FilterUtil() {
    }

    /**
     * Apply the given filter to the given model.
     *
     * @param filter
     * @param model
     */
    public static final OpenAPI applyFilter(OASFilter filter, OpenAPI model) {
        filterComponents(filter, model.getComponents());
        filterExtensions(filter, model.getExtensions());
        filterExternalDocs(filter, model.getExternalDocs());
        filterInfo(filter, model.getInfo());
        filterPaths(filter, model.getPaths());
        filterSecurity(filter, model.getSecurity());
        filterServers(filter, model.getServers());
        filterTags(filter, model.getTags());

        filter.filterOpenAPI(model);
        return model;
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterComponents(OASFilter filter, Components model) {
        if (model == null) {
            return;
        }
        filterCallbacks(filter, model.getCallbacks());
        filterExamples(filter, model.getExamples());
        filterExtensions(filter, model.getExtensions());
        filterHeaders(filter, model.getHeaders());
        filterLinks(filter, model.getLinks());
        filterParameters(filter, model.getParameters());
        filterRequestBodies(filter, model.getRequestBodies());
        filterAPIResponses(filter, model.getResponses());
        filterSchemas(filter, model.getSchemas());
        filterSecuritySchemes(filter, model.getSecuritySchemes());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterCallbacks(OASFilter filter, Map<String, Callback> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            Callback model = models.get(key);
            filterCallback(filter, model);

            if (filter.filterCallback(model) == null) {
                models.remove(key);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterCallback(OASFilter filter, Callback model) {
        if (model == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(model.keySet());
        for (String key : keys) {
            PathItem childModel = model.get(key);
            filterPathItem(filter, childModel);

            if (filter.filterPathItem(childModel) == null) {
                model.remove(key);
            }
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterPathItem(OASFilter filter, PathItem model) {
        if (model == null) {
            return;
        }
        filterParameterList(filter, model.getParameters());
        filterOperation(filter, model.getDELETE());
        if (model.getDELETE() != null) {
            model.setDELETE(filter.filterOperation(model.getDELETE()));
        }
        filterOperation(filter, model.getGET());
        if (model.getGET() != null) {
            model.setGET(filter.filterOperation(model.getGET()));
        }
        filterOperation(filter, model.getHEAD());
        if (model.getHEAD() != null) {
            model.setHEAD(filter.filterOperation(model.getHEAD()));
        }
        filterOperation(filter, model.getOPTIONS());
        if (model.getOPTIONS() != null) {
            model.setOPTIONS(filter.filterOperation(model.getOPTIONS()));
        }
        filterOperation(filter, model.getPATCH());
        if (model.getPATCH() != null) {
            model.setPATCH(filter.filterOperation(model.getPATCH()));
        }
        filterOperation(filter, model.getPOST());
        if (model.getPOST() != null) {
            model.setPOST(filter.filterOperation(model.getPOST()));
        }
        filterOperation(filter, model.getPUT());
        if (model.getPUT() != null) {
            model.setPUT(filter.filterOperation(model.getPUT()));
        }
        filterOperation(filter, model.getTRACE());
        if (model.getTRACE() != null) {
            model.setTRACE(filter.filterOperation(model.getTRACE()));
        }
        filterServers(filter, model.getServers());
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterParameterList(OASFilter filter, List<Parameter> models) {
        if (models == null) {
            return;
        }
        ListIterator<Parameter> iterator = models.listIterator();
        while (iterator.hasNext()) {
            Parameter model = iterator.next();
            filterParameter(filter, model);

            if (filter.filterParameter(model) == null) {
                iterator.remove();
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterOperation(OASFilter filter, Operation model) {
        if (model == null) {
            return;
        }
        filterCallbacks(filter, model.getCallbacks());
        filterExtensions(filter, model.getExtensions());
        filterExternalDocs(filter, model.getExternalDocs());
        filterParameterList(filter, model.getParameters());
        filterRequestBody(filter, model.getRequestBody());
        if (model.getRequestBody() != null && filter.filterRequestBody(model.getRequestBody()) == null) {
            model.setRequestBody(null);
        }
        filterAPIResponses(filter, model.getResponses());
        filterSecurity(filter, model.getSecurity());
        filterServers(filter, model.getServers());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterExamples(OASFilter filter, Map<String, Example> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            Example model = models.get(key);
            filterExample(filter, model);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterExample(OASFilter filter, Example model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterHeaders(OASFilter filter, Map<String, Header> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            Header model = models.get(key);
            filterHeader(filter, model);

            if (filter.filterHeader(model) == null) {
                models.remove(key);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterHeader(OASFilter filter, Header model) {
        if (model == null) {
            return;
        }
        filterContent(filter, model.getContent());
        filterExamples(filter, model.getExamples());
        filterExtensions(filter, model.getExtensions());
        filterSchema(filter, model.getSchema());
        if (model.getSchema() != null && filter.filterSchema(model.getSchema()) == null) {
            model.setSchema(null);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterContent(OASFilter filter, Content model) {
        if (model == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(model.keySet());
        for (String key : keys) {
            MediaType childModel = model.get(key);
            filterMediaType(filter, childModel);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterMediaType(OASFilter filter, MediaType model) {
        if (model == null) {
            return;
        }
        filterEncoding(filter, model.getEncoding());
        filterExamples(filter, model.getExamples());
        filterExtensions(filter, model.getExtensions());
        filterSchema(filter, model.getSchema());
        if (model.getSchema() != null && filter.filterSchema(model.getSchema()) == null) {
            model.setSchema(null);
        }
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterEncoding(OASFilter filter, Map<String, Encoding> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            Encoding model = models.get(key);
            filterEncoding(filter, model);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterEncoding(OASFilter filter, Encoding model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
        filterHeaders(filter, model.getHeaders());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterLinks(OASFilter filter, Map<String, Link> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            Link model = models.get(key);
            filterLink(filter, model);

            if (filter.filterLink(model) == null) {
                models.remove(key);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterLink(OASFilter filter, Link model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
        filterServer(filter, model.getServer());
        if (model.getServer() != null && filter.filterServer(model.getServer()) == null) {
            model.setServer(null);
        }
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterParameters(OASFilter filter, Map<String, Parameter> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            Parameter model = models.get(key);
            filterParameter(filter, model);

            if (filter.filterParameter(model) == null) {
                models.remove(key);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterParameter(OASFilter filter, Parameter model) {
        if (model == null) {
            return;
        }
        filterContent(filter, model.getContent());
        filterExamples(filter, model.getExamples());
        filterExtensions(filter, model.getExtensions());
        filterSchema(filter, model.getSchema());
        if (model.getSchema() != null && filter.filterSchema(model.getSchema()) == null) {
            model.setSchema(null);
        }
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterRequestBodies(OASFilter filter, Map<String, RequestBody> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            RequestBody model = models.get(key);
            filterRequestBody(filter, model);

            if (filter.filterRequestBody(model) == null) {
                models.remove(key);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterRequestBody(OASFilter filter, RequestBody model) {
        if (model == null) {
            return;
        }
        filterContent(filter, model.getContent());
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterAPIResponses(OASFilter filter, Map<String, APIResponse> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            APIResponse model = models.get(key);
            filterAPIResponse(filter, model);

            if (filter.filterAPIResponse(model) == null) {
                models.remove(key);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterAPIResponse(OASFilter filter, APIResponse model) {
        if (model == null) {
            return;
        }
        filterContent(filter, model.getContent());
        filterExtensions(filter, model.getExtensions());
        filterHeaders(filter, model.getHeaders());
        filterLinks(filter, model.getLinks());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterSchemas(OASFilter filter, Map<String, Schema> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            Schema model = models.get(key);
            filterSchema(filter, model);

            if (filter.filterSchema(model) == null) {
                models.remove(key);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterSchema(OASFilter filter, Schema model) {
        if (model == null) {
            return;
        }
        Object ap = model.getAdditionalProperties();
        if (ap != null && ap instanceof Schema) {
            filterSchema(filter, (Schema) ap);
            if (filter.filterSchema((Schema) ap) == null) {
                model.setAdditionalProperties((Schema) null);
            }
        }
        filterSchemaList(filter, model.getAllOf());
        filterSchemaList(filter, model.getAnyOf());
        filterDiscriminator(filter, model.getDiscriminator());
        filterExtensions(filter, model.getExtensions());
        filterExternalDocs(filter, model.getExternalDocs());
        filterSchema(filter, model.getItems());
        if (model.getItems() != null && filter.filterSchema(model.getItems()) == null) {
            model.setItems(null);
        }
        filterSchema(filter, model.getNot());
        if (model.getNot() != null && filter.filterSchema(model.getNot()) == null) {
            model.setNot(null);
        }
        filterSchemas(filter, model.getProperties());
        filterXML(filter, model.getXml());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterSchemaList(OASFilter filter, List<Schema> models) {
        if (models == null) {
            return;
        }
        ListIterator<Schema> iterator = models.listIterator();
        while (iterator.hasNext()) {
            Schema model = iterator.next();
            filterSchema(filter, model);

            if (filter.filterSchema(model) == null) {
                iterator.remove();
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterDiscriminator(OASFilter filter, Discriminator model) {
        if (model == null) {
            return;
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterXML(OASFilter filter, XML model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private static void filterSecuritySchemes(OASFilter filter, Map<String, SecurityScheme> models) {
        if (models == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(models.keySet());
        for (String key : keys) {
            SecurityScheme model = models.get(key);
            filterSecurityScheme(filter, model);

            if (filter.filterSecurityScheme(model) == null) {
                models.remove(key);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterSecurityScheme(OASFilter filter, SecurityScheme model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
        filterOAuthFlows(filter, model.getFlows());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterOAuthFlows(OASFilter filter, OAuthFlows model) {
        if (model == null) {
            return;
        }
        filterOAuthFlow(filter, model.getAuthorizationCode());
        filterOAuthFlow(filter, model.getClientCredentials());
        filterExtensions(filter, model.getExtensions());
        filterOAuthFlow(filter, model.getImplicit());
        filterOAuthFlow(filter, model.getPassword());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterOAuthFlow(OASFilter filter, OAuthFlow model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
        filterScopes(filter, model.getScopes());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterScopes(OASFilter filter, Scopes model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param models
     */
    private static void filterExtensions(OASFilter filter, Map<String, Object> models) {
        if (models == null) {
            return;
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterExternalDocs(OASFilter filter, ExternalDocumentation model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterInfo(OASFilter filter, Info model) {
        if (model == null) {
            return;
        }
        filterContact(filter, model.getContact());
        filterExtensions(filter, model.getExtensions());
        filterLicense(filter, model.getLicense());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterContact(OASFilter filter, Contact model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * @param filter
     * @param model
     */
    private static void filterLicense(OASFilter filter, License model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterPaths(OASFilter filter, Paths model) {
        if (model == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(model.keySet());
        for (String key : keys) {
            PathItem childModel = model.get(key);
            filterPathItem(filter, childModel);

            if (filter.filterPathItem(childModel) == null) {
                model.remove(key);
            }
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param models
     */
    private static void filterSecurity(OASFilter filter, List<SecurityRequirement> models) {
        if (models == null) {
            return;
        }
        ListIterator<SecurityRequirement> iterator = models.listIterator();
        while (iterator.hasNext()) {
            SecurityRequirement model = iterator.next();
            filterSecurityRequirement(filter, model);
        }

    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterSecurityRequirement(OASFilter filter, SecurityRequirement model) {
        if (model == null) {
            return;
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param models
     */
    private static void filterServers(OASFilter filter, List<Server> models) {
        if (models == null) {
            return;
        }
        ListIterator<Server> iterator = models.listIterator();
        while (iterator.hasNext()) {
            Server model = iterator.next();
            filterServer(filter, model);

            if (filter.filterServer(model) == null) {
                iterator.remove();
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterServer(OASFilter filter, Server model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
        filterServerVariables(filter, model.getVariables());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterServerVariables(OASFilter filter, ServerVariables model) {
        if (model == null) {
            return;
        }
        Collection<String> keys = new ArrayList<>(model.keySet());
        for (String key : keys) {
            ServerVariable childModel = model.get(key);
            filterServerVariable(filter, childModel);
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterServerVariable(OASFilter filter, ServerVariable model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param models
     */
    private static void filterTags(OASFilter filter, List<Tag> models) {
        if (models == null) {
            return;
        }
        ListIterator<Tag> iterator = models.listIterator();
        while (iterator.hasNext()) {
            Tag model = iterator.next();
            filterTag(filter, model);

            model = filter.filterTag(model);
            if (model == null) {
                iterator.remove();
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private static void filterTag(OASFilter filter, Tag model) {
        if (model == null) {
            return;
        }
        filterExtensions(filter, model.getExtensions());
        filterExternalDocs(filter, model.getExternalDocs());
    }

}
