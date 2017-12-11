/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.openapi.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.Extensible;
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
import org.eclipse.microprofile.openapi.models.media.Encoding.Style;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
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
import org.eclipse.microprofile.openapi.models.security.SecurityScheme.In;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.wildfly.swarm.microprofile.openapi.models.ComponentsImpl;
import org.wildfly.swarm.microprofile.openapi.models.ExternalDocumentationImpl;
import org.wildfly.swarm.microprofile.openapi.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.models.OperationImpl;
import org.wildfly.swarm.microprofile.openapi.models.PathItemImpl;
import org.wildfly.swarm.microprofile.openapi.models.PathsImpl;
import org.wildfly.swarm.microprofile.openapi.models.callbacks.CallbackImpl;
import org.wildfly.swarm.microprofile.openapi.models.examples.ExampleImpl;
import org.wildfly.swarm.microprofile.openapi.models.headers.HeaderImpl;
import org.wildfly.swarm.microprofile.openapi.models.info.ContactImpl;
import org.wildfly.swarm.microprofile.openapi.models.info.InfoImpl;
import org.wildfly.swarm.microprofile.openapi.models.info.LicenseImpl;
import org.wildfly.swarm.microprofile.openapi.models.links.LinkImpl;
import org.wildfly.swarm.microprofile.openapi.models.media.ContentImpl;
import org.wildfly.swarm.microprofile.openapi.models.media.DiscriminatorImpl;
import org.wildfly.swarm.microprofile.openapi.models.media.EncodingImpl;
import org.wildfly.swarm.microprofile.openapi.models.media.MediaTypeImpl;
import org.wildfly.swarm.microprofile.openapi.models.media.SchemaImpl;
import org.wildfly.swarm.microprofile.openapi.models.media.XMLImpl;
import org.wildfly.swarm.microprofile.openapi.models.parameters.CookieParameterImpl;
import org.wildfly.swarm.microprofile.openapi.models.parameters.HeaderParameterImpl;
import org.wildfly.swarm.microprofile.openapi.models.parameters.PathParameterImpl;
import org.wildfly.swarm.microprofile.openapi.models.parameters.QueryParameterImpl;
import org.wildfly.swarm.microprofile.openapi.models.parameters.RequestBodyImpl;
import org.wildfly.swarm.microprofile.openapi.models.responses.APIResponseImpl;
import org.wildfly.swarm.microprofile.openapi.models.responses.APIResponsesImpl;
import org.wildfly.swarm.microprofile.openapi.models.security.OAuthFlowImpl;
import org.wildfly.swarm.microprofile.openapi.models.security.OAuthFlowsImpl;
import org.wildfly.swarm.microprofile.openapi.models.security.ScopesImpl;
import org.wildfly.swarm.microprofile.openapi.models.security.SecurityRequirementImpl;
import org.wildfly.swarm.microprofile.openapi.models.security.SecuritySchemeImpl;
import org.wildfly.swarm.microprofile.openapi.models.servers.ServerImpl;
import org.wildfly.swarm.microprofile.openapi.models.servers.ServerVariableImpl;
import org.wildfly.swarm.microprofile.openapi.models.servers.ServerVariablesImpl;
import org.wildfly.swarm.microprofile.openapi.models.tags.TagImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * A class used to parse an OpenAPI document (either YAML or JSON) into a Microprofile OpenAPI model tree.
 * @author eric.wittmann@gmail.com
 */
public class OpenApiParser {

    private static final Map<String, Style> ENCODING_STYLE_LOOKUP = new HashMap<>();
    private static final Map<String, org.eclipse.microprofile.openapi.models.parameters.Parameter.Style> PARAMETER_STYLE_LOOKUP = new HashMap<>();
    private static final Map<String, org.eclipse.microprofile.openapi.models.headers.Header.Style> HEADER_STYLE_LOOKUP = new HashMap<>();
    private static final Map<String, Type> SECURITY_SCHEME_TYPE_LOOKUP = new HashMap<>();
    private static final Map<String, In> SECURITY_SCHEME_IN_LOOKUP = new HashMap<>();

    static {
        Style[] encodingStyleValues = Style.values();
        for (Style style : encodingStyleValues) {
            ENCODING_STYLE_LOOKUP.put(style.toString(), style);
        }

        org.eclipse.microprofile.openapi.models.parameters.Parameter.Style[] parameterStyleValues = org.eclipse.microprofile.openapi.models.parameters.Parameter.Style.values();
        for (org.eclipse.microprofile.openapi.models.parameters.Parameter.Style style : parameterStyleValues) {
            PARAMETER_STYLE_LOOKUP.put(style.toString(), style);
        }

        org.eclipse.microprofile.openapi.models.headers.Header.Style[] headerStyleValues = org.eclipse.microprofile.openapi.models.headers.Header.Style.values();
        for (org.eclipse.microprofile.openapi.models.headers.Header.Style style : headerStyleValues) {
            HEADER_STYLE_LOOKUP.put(style.toString(), style);
        }

        Type[] securitySchemeTypes = Type.values();
        for (Type type : securitySchemeTypes) {
            SECURITY_SCHEME_TYPE_LOOKUP.put(type.toString(), type);
        }

        In[] securitySchemeIns = In.values();
        for (In type : securitySchemeIns) {
            SECURITY_SCHEME_IN_LOOKUP.put(type.toString(), type);
        }

    }

    /**
     * Parses the resource found at the given URL.  This method accepts resources
     * either in JSON or YAML format.  It will parse the input and, assuming it is
     * valid, return an instance of {@link OpenAPI}.
     * @param url
     */
    public static final OpenAPIImpl parse(URL url) throws IOException, ParseException {
        try {
            String fname = url.getFile();
            if (fname == null) {
                throw new IOException("No file name for URL: " + url.toURI().toString());
            }
            int lidx = fname.lastIndexOf('.');
            if (lidx == -1 || lidx >= fname.length()) {
                throw new IOException("Invalid file name for URL: " + url.toURI().toString());
            }
            String ext = fname.substring(lidx + 1);
            boolean isJson = ext.equalsIgnoreCase("json");
            boolean isYaml = ext.equalsIgnoreCase("yaml") || ext.equalsIgnoreCase("yml");

            ObjectMapper mapper;
            if (isJson) {
                mapper = new ObjectMapper();
            } else if (isYaml) {
                mapper = new ObjectMapper(new YAMLFactory());
            } else {
                throw new IOException("Invalid file extension for URL (expected json, yaml, or yml): " + url.toURI().toString());
            }
            JsonNode tree = mapper.readTree(url);

            OpenApiParser parser = new OpenApiParser(tree);
            return parser.parse();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private final JsonNode tree;

    /**
     * Constructor.
     * @param tree
     */
    public OpenApiParser(JsonNode tree) {
        this.tree = tree;
    }

    /**
     * Parses the json tree into an OpenAPI data model.
     */
    private OpenAPIImpl parse() {
        OpenAPIImpl oai = new OpenAPIImpl();
        this.readOpenAPI(tree, oai);
        return oai;
    }

    /**
     * Reads the root OpenAPI node.
     * @param node
     * @param model
     */
    private void readOpenAPI(JsonNode node, OpenAPIImpl model) {
        model.setOpenapi(JsonUtil.stringProperty(node, "openapi"));
        model.setInfo(readInfo(node.get("info")));
        model.setExternalDocs(readExternalDocs(node.get("externalDocs")));
        model.setServers(readServers(node.get("servers")));
        model.setSecurity(readSecurityRequirements(node.get("security")));
        model.setTags(readTags(node.get("tags")));
        model.setPaths(readPaths(node.get("paths")));
        model.setComponents(readComponents(node.get("components")));
        readExtensions(node, model);
    }

    /**
     * Reads an {@link Info} OpenAPI node.
     * @param node
     */
    private Info readInfo(JsonNode node) {
        if (node == null) {
            return null;
        }
        InfoImpl model = new InfoImpl();
        model.setTitle(JsonUtil.stringProperty(node, "title"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setTermsOfService(JsonUtil.stringProperty(node, "termsOfService"));
        model.setContact(readContact(node.get("contact")));
        model.setLicense(readLicense(node.get("license")));
        model.setVersion(JsonUtil.stringProperty(node, "version"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads an {@link Contact} OpenAPI node.
     * @param node
     */
    private Contact readContact(JsonNode node) {
        if (node == null) {
            return null;
        }
        ContactImpl model = new ContactImpl();
        model.setName(JsonUtil.stringProperty(node, "name"));
        model.setUrl(JsonUtil.stringProperty(node, "url"));
        model.setEmail(JsonUtil.stringProperty(node, "email"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads an {@link License} OpenAPI node.
     * @param node
     */
    private License readLicense(JsonNode node) {
        if (node == null) {
            return null;
        }
        LicenseImpl model = new LicenseImpl();
        model.setName(JsonUtil.stringProperty(node, "name"));
        model.setUrl(JsonUtil.stringProperty(node, "url"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads an {@link ExternalDocumentation} OpenAPI node.
     * @param node
     */
    private ExternalDocumentation readExternalDocs(JsonNode node) {
        if (node == null) {
            return null;
        }
        ExternalDocumentationImpl model = new ExternalDocumentationImpl();
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setUrl(JsonUtil.stringProperty(node, "url"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a list of {@link Tag} OpenAPI nodes.
     * @param node
     */
    private List<Tag> readTags(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        ArrayNode nodes = (ArrayNode) node;
        List<Tag> rval = new ArrayList<>(nodes.size());
        for (JsonNode tagNode : nodes) {
            TagImpl model = new TagImpl();
            model.setName(JsonUtil.stringProperty(tagNode, "name"));
            model.setDescription(JsonUtil.stringProperty(tagNode, "description"));
            model.setExternalDocs(readExternalDocs(tagNode.get("externalDocs")));
            readExtensions(tagNode, model);
            rval.add(model);
        }
        return rval;
    }

    /**
     * Reads a list of {@link Server} OpenAPI nodes.
     * @param node
     */
    private List<Server> readServers(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        ArrayNode nodes = (ArrayNode) node;
        List<Server> rval = new ArrayList<>(nodes.size());
        for (JsonNode serverNode : nodes) {
            ServerImpl model = new ServerImpl();
            model.setUrl(JsonUtil.stringProperty(serverNode, "url"));
            model.setDescription(JsonUtil.stringProperty(serverNode, "description"));
            model.setVariables(readServerVariables(serverNode.get("variables")));
            readExtensions(serverNode, model);
            rval.add(model);
        }
        return rval;
    }

    /**
     * Reads the {@link ServerVariables} OpenAPI node.
     * @param node
     */
    private ServerVariables readServerVariables(JsonNode node) {
        if (node == null) {
            return null;
        }
        ServerVariablesImpl model = new ServerVariablesImpl();

        for (Iterator<String> iterator = node.fieldNames(); iterator.hasNext();) {
            String fieldName = iterator.next();
            if (!fieldName.toLowerCase().startsWith("x-")) {
                JsonNode varNode = node.get(fieldName);
                ServerVariable varModel = readServerVariable(varNode);
                model.put(fieldName, varModel);
            }
        }

        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a list of {@link ServerVariable} OpenAPI nodes.
     * @param node
     */
    private ServerVariable readServerVariable(JsonNode node) {
        if (node == null) {
            return null;
        }
        ServerVariableImpl model = new ServerVariableImpl();
        JsonNode enumNode = node.get("enum");
        if (enumNode != null && enumNode.isArray()) {
            List<String> enums = new ArrayList<>(enumNode.size());
            for (JsonNode n : enumNode) {
                enums.add(n.asText());
            }
            model.setEnumeration(enums);
        }
        model.setDefaultValue(JsonUtil.stringProperty(node, "default"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads the {@link Paths} OpenAPI nodes.
     * @param node
     */
    private Paths readPaths(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        PathsImpl model = new PathsImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            if (fieldName.startsWith("x-")) {
                continue;
            }
            model.addPathItem(fieldName, readPathItem(node.get(fieldName)));
        }
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads the {@link Components} OpenAPI nodes.
     * @param node
     */
    private Components readComponents(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        ComponentsImpl model = new ComponentsImpl();
        model.setSchemas(readSchemas(node.get("schemas")));
        model.setResponses(readResponses(node.get("responses")));
        model.setParameters(readParameters(node.get("parameters")));
        model.setExamples(readExamples(node.get("examples")));
        model.setRequestBodies(readRequestBodies(node.get("requestBodies")));
        model.setHeaders(readHeaders(node.get("headers")));
        model.setSecuritySchemes(readSecuritySchemes(node.get("securitySchemes")));
        model.setLinks(readLinks(node.get("links")));
        model.setCallbacks(readCallbacks(node.get("callbacks")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads the {@link Schema} OpenAPI nodes.
     * @param node
     */
    private Map<String, Schema> readSchemas(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Schema> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readSchema(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link Schema} OpenAPI node.
     * @param node
     */
    private Schema readSchema(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        SchemaImpl model = new SchemaImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));
        model.setFormat(JsonUtil.stringProperty(node, "format"));
        model.setTitle(JsonUtil.stringProperty(node, "title"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setDefaultValue(readObject(node.get("default")));
        model.setMultipleOf(JsonUtil.bigDecimalProperty(node, "multipleOf"));
        model.setMaximum(JsonUtil.bigDecimalProperty(node, "maximum"));
        model.setExclusiveMaximum(JsonUtil.booleanProperty(node, "exclusiveMaximum"));
        model.setMinimum(JsonUtil.bigDecimalProperty(node, "minimum"));
        model.setExclusiveMinimum(JsonUtil.booleanProperty(node, "exclusiveMinimum"));
        model.setMaxLength(JsonUtil.intProperty(node, "maxLength"));
        model.setMinLength(JsonUtil.intProperty(node, "minLength"));
        model.setPattern(JsonUtil.stringProperty(node, "pattern"));
        model.setMaxItems(JsonUtil.intProperty(node, "maxItems"));
        model.setMinItems(JsonUtil.intProperty(node, "minItems"));
        model.setUniqueItems(JsonUtil.booleanProperty(node, "uniqueItems"));
        model.setMaxProperties(JsonUtil.intProperty(node, "maxProperties"));
        model.setMinProperties(JsonUtil.intProperty(node, "minProperties"));
        model.setRequired(readStringArray(node.get("required")));
        // TODO enum can be something other than a string - handle that!
        model.setEnumeration(readObjectArray(node.get("enum")));
        model.setType(readSchemaType(node.get("type")));
        model.setItems(readSchema(node.get("items")));
        model.setNot(readSchema(node.get("not")));
        model.setAllOf(readSchemaArray(node.get("allOf")));
        model.setProperties(readSchemas(node.get("properties")));
        if (node.has("additionalProperties") && node.get("additionalProperties").isObject()) {
            model.setAdditionalProperties(readSchema(node.get("additionalProperties")));
        } else {
            model.setAdditionalProperties(JsonUtil.booleanProperty(node, "additionalProperties"));
        }
        model.setReadOnly(JsonUtil.booleanProperty(node, "readOnly"));
        model.setXml(readXML(node.get("xml")));
        model.setExternalDocs(readExternalDocs(node.get("externalDocs")));
        model.setExample(readObject(node.get("example")));
        model.setOneOf(readSchemaArray(node.get("oneOf")));
        model.setAnyOf(readSchemaArray(node.get("anyOf")));
        model.setNot(readSchema(node.get("not")));
        model.setDiscriminator(readDiscriminator(node.get("discriminator")));
        model.setNullable(JsonUtil.booleanProperty(node, "nullable"));
        model.setWriteOnly(JsonUtil.booleanProperty(node, "writeOnly"));
        model.setDeprecated(JsonUtil.booleanProperty(node, "deprecated"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a {@link XML} OpenAPI node.
     * @param node
     */
    private XML readXML(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        XMLImpl model = new XMLImpl();
        model.setName(JsonUtil.stringProperty(node, "name"));
        model.setNamespace(JsonUtil.stringProperty(node, "namespace"));
        model.setPrefix(JsonUtil.stringProperty(node, "prefix"));
        model.setAttribute(JsonUtil.booleanProperty(node, "attribute"));
        model.setWrapped(JsonUtil.booleanProperty(node, "wrapped"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a {@link Discriminator} OpenAPI node.
     * @param node
     */
    private Discriminator readDiscriminator(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        DiscriminatorImpl model = new DiscriminatorImpl();
        model.setPropertyName(JsonUtil.stringProperty(node, "propertyName"));
        model.setMapping(readStringMap(node.get("mapping")));
        return model;
    }

    /**
     * Reads the {@link APIResponse} OpenAPI nodes.
     * @param node
     */
    private Map<String, APIResponse> readResponses(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, APIResponse> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readAPIResponse(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link APIResponse} OpenAPI node.
     * @param node
     */
    private APIResponse readAPIResponse(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        APIResponseImpl model = new APIResponseImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setHeaders(readHeaders(node.get("headers")));
        model.setContent(readContent(node.get("content")));
        model.setLinks(readLinks(node.get("links")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a {@link Content} OpenAPI node.
     * @param node
     */
    private Content readContent(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        ContentImpl model = new ContentImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            model.addMediaType(fieldName, readMediaType(node.get(fieldName)));
        }
        return model;
    }

    /**
     * Reads a {@link MediaType} OpenAPI node.
     * @param node
     * @return
     */
    private MediaType readMediaType(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        MediaTypeImpl model = new MediaTypeImpl();
        model.setSchema(readSchema(node.get("schema")));
        model.setExample(readObject(node.get("example")));
        model.setExamples(readExamples(node.get("examples")));
        model.setEncoding(readEncodings(node.get("encoding")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a map of {@link MediaType} OpenAPI nodes.
     * @param node
     */
    private Map<String, Encoding> readEncodings(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Encoding> encodings = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String name = fieldNames.next();
            encodings.put(name, readEncoding(node.get(name)));
        }
        return encodings;
    }

    /**
     * Reads a {@link Encoding} OpenAPI node.
     * @param node
     */
    private Encoding readEncoding(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        EncodingImpl model = new EncodingImpl();
        model.setContentType(JsonUtil.stringProperty(node, "contentType"));
        model.setHeaders(readHeaders(node.get("headers")));
        model.setStyle(readEncodingStyle(node.get("style")));
        model.setExplode(JsonUtil.booleanProperty(node, "explode"));
        model.setAllowReserved(JsonUtil.booleanProperty(node, "allowReserved"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads an encoding style.
     * @param node
     */
    private Style readEncodingStyle(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }

        return ENCODING_STYLE_LOOKUP.get(node.asText());
    }

    /**
     * Reads the {@link Parameter} OpenAPI nodes.
     * @param node
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Parameter> readParameters(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Parameter> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readParameter(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link Parameter} OpenAPI node.
     * @param node
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Parameter readParameter(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Parameter model = null;
        if (node.has("in")) {
            String in = node.get("in").asText();
            if ("cookie".equals(in)) {
                model = new CookieParameterImpl();
            }
            if ("header".equals(in)) {
                model = new HeaderParameterImpl();
            }
            if ("path".equals(in)) {
                model = new PathParameterImpl();
            }
            if ("query".equals(in)) {
                model = new QueryParameterImpl();
            }
        }

        if (model != null) {
            model.setRef(JsonUtil.stringProperty(node, "$ref"));
            model.setName(JsonUtil.stringProperty(node, "name"));
            model.setDescription(JsonUtil.stringProperty(node, "description"));
            model.setRequired(JsonUtil.booleanProperty(node, "required"));
            model.setSchema(readSchema(node.get("schema")));
            model.setAllowEmptyValue(JsonUtil.booleanProperty(node, "allowEmptyValue"));
            model.setDeprecated(JsonUtil.booleanProperty(node, "deprecated"));
            model.setStyle(readParameterStyle(node.get("style")));
            model.setExplode(JsonUtil.booleanProperty(node, "explode"));
            model.setAllowReserved(JsonUtil.booleanProperty(node, "allowReserved"));
            model.setExample(readObject(node.get("example")));
            model.setExamples(readExamples(node.get("examples")));
            model.setContent(readContent(node.get("content")));
            readExtensions(node, model);
        }

        return model;
    }

    /**
     * Reads a parameter style.
     * @param node
     */
    private org.eclipse.microprofile.openapi.models.parameters.Parameter.Style readParameterStyle(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return PARAMETER_STYLE_LOOKUP.get(node.asText());
    }

    /**
     * Reads the {@link Example} OpenAPI nodes.
     * @param node
     */
    private Map<String, Example> readExamples(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Example> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readExample(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link Example} OpenAPI node.
     * @param node
     */
    private Example readExample(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        ExampleImpl model = new ExampleImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));
        model.setSummary(JsonUtil.stringProperty(node, "summary"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setValue(readObject(node.get("value")));
        model.setExternalValue(JsonUtil.stringProperty(node, "externalValue"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads the {@link RequestBody} OpenAPI nodes.
     * @param node
     */
    private Map<String, RequestBody> readRequestBodies(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, RequestBody> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readRequestBody(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link RequestBody} OpenAPI node.
     * @param node
     */
    private RequestBody readRequestBody(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        RequestBodyImpl model = new RequestBodyImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setContent(readContent(node.get("content")));
        model.setRequired(JsonUtil.booleanProperty(node, "required"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads the {@link Header} OpenAPI nodes.
     * @param node
     */
    private Map<String, Header> readHeaders(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Header> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readHeader(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link Header} OpenAPI node.
     * @param node
     */
    private Header readHeader(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        HeaderImpl model = new HeaderImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setRequired(JsonUtil.booleanProperty(node, "required"));
        model.setDeprecated(JsonUtil.booleanProperty(node, "deprecated"));
        model.setAllowEmptyValue(JsonUtil.booleanProperty(node, "allowEmptyValue"));
        model.setStyle(readHeaderStyle(node.get("style")));
        model.setExplode(JsonUtil.booleanProperty(node, "explode"));
        model.setSchema(readSchema(node.get("schema")));
        model.setExample(readObject(node.get("example")));
        model.setExamples(readExamples(node.get("examples")));
        model.setContent(readContent(node.get("content")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a header style.
     * @param node
     */
    private org.eclipse.microprofile.openapi.models.headers.Header.Style readHeaderStyle(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return HEADER_STYLE_LOOKUP.get(node.asText());
    }

    /**
     * Reads the {@link SecurityScheme} OpenAPI nodes.
     * @param node
     */
    private Map<String, SecurityScheme> readSecuritySchemes(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, SecurityScheme> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readSecurityScheme(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link SecurityScheme} OpenAPI node.
     * @param node
     */
    private SecurityScheme readSecurityScheme(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        SecuritySchemeImpl model = new SecuritySchemeImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));
        model.setType(readSecuritySchemeType(node.get("type")));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setName(JsonUtil.stringProperty(node, "name"));
        model.setIn(readSecuritySchemeIn(node.get("in")));
        model.setScheme(JsonUtil.stringProperty(node, "scheme"));
        model.setBearerFormat(JsonUtil.stringProperty(node, "bearerFormat"));
        model.setFlows(readOAuthFlows(node.get("flows")));
        model.setOpenIdConnectUrl(JsonUtil.stringProperty(node, "openIdConnectUrl"));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a security scheme type.
     * @param node
     */
    private Type readSecuritySchemeType(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return SECURITY_SCHEME_TYPE_LOOKUP.get(node.asText());
    }

    /**
     * Reads a security scheme 'in' property.
     * @param node
     */
    private In readSecuritySchemeIn(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return SECURITY_SCHEME_IN_LOOKUP.get(node.asText());
    }

    /**
     * Reads a {@link OAuthFlows} OpenAPI node.
     * @param node
     */
    private OAuthFlows readOAuthFlows(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        OAuthFlowsImpl model = new OAuthFlowsImpl();
        model.setImplicit(readOAuthFlow(node.get("implicit")));
        model.setPassword(readOAuthFlow(node.get("password")));
        model.setClientCredentials(readOAuthFlow(node.get("clientCredentials")));
        model.setAuthorizationCode(readOAuthFlow(node.get("authorizationCode")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a {@link OAuthFlow} OpenAPI node.
     * @param node
     */
    private OAuthFlow readOAuthFlow(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        OAuthFlowImpl model = new OAuthFlowImpl();
        model.setAuthorizationUrl(JsonUtil.stringProperty(node, "authorizationUrl"));
        model.setTokenUrl(JsonUtil.stringProperty(node, "tokenUrl"));
        model.setRefreshUrl(JsonUtil.stringProperty(node, "refreshUrl"));
        model.setScopes(readScopes(node.get("scopes")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a {@link Scopes} OpenAPI node.
     * @param node
     */
    private Scopes readScopes(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        ScopesImpl model = new ScopesImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            if (fieldName.startsWith("x-")) {
                continue;
            }
            String value = JsonUtil.stringProperty(node, fieldName);
            model.put(fieldName, value);
        }
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads the {@link Link} OpenAPI nodes.
     * @param node
     */
    private Map<String, Link> readLinks(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Link> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readLink(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link Link} OpenAPI node.
     * @param node
     */
    private Link readLink(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LinkImpl model = new LinkImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));

        model.setOperationRef(JsonUtil.stringProperty(node, "operationRef"));
        model.setOperationId(JsonUtil.stringProperty(node, "operationId"));
        model.setParameters(readLinkParameters(node.get("parameters")));
        model.setRequestBody(readObject(node.get("requestBody")));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setServer(readServer(node.get("server")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads the map of {@link Link} parameters.
     * @param node
     */
    private Map<String, Object> readLinkParameters(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Object> rval = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            Object value = readObject(node.get(fieldName));
            rval.put(fieldName, value);
        }
        return rval;
    }

    /**
     * Reads a {@link Server} OpenAPI node.
     * @param node
     */
    private Server readServer(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        ServerImpl model = new ServerImpl();
        model.setUrl(JsonUtil.stringProperty(node, "url"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setVariables(readServerVariables(node.get("variables")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads the {@link Callback} OpenAPI nodes.
     * @param node
     */
    private Map<String, Callback> readCallbacks(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Callback> models = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            models.put(fieldName, readCallback(childNode));
        }

        return models;
    }

    /**
     * Reads a {@link Callback} OpenAPI node.
     * @param node
     */
    private Callback readCallback(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        CallbackImpl model = new CallbackImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            if (fieldName.startsWith("x-") || fieldName.equals("$ref")) {
                continue;
            }
            model.put(fieldName, readPathItem(node.get(fieldName)));
        }
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a {@link PathItem} OpenAPI node.
     * @param node
     */
    private PathItem readPathItem(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        PathItemImpl model = new PathItemImpl();
        model.setRef(JsonUtil.stringProperty(node, "$ref"));
        model.setSummary(JsonUtil.stringProperty(node, "summary"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setGET(readOperation(node.get("get")));
        model.setPUT(readOperation(node.get("put")));
        model.setPOST(readOperation(node.get("post")));
        model.setDELETE(readOperation(node.get("delete")));
        model.setOPTIONS(readOperation(node.get("options")));
        model.setHEAD(readOperation(node.get("head")));
        model.setPATCH(readOperation(node.get("patch")));
        model.setTRACE(readOperation(node.get("trace")));
        model.setParameters(readParameterList(node.get("parameters")));
        model.setServers(readServers(node.get("servers")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a {@link Operation} OpenAPI node.
     * @param node
     */
    private Operation readOperation(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        OperationImpl model = new OperationImpl();
        model.setTags(readStringArray(node.get("tags")));
        model.setSummary(JsonUtil.stringProperty(node, "summary"));
        model.setDescription(JsonUtil.stringProperty(node, "description"));
        model.setExternalDocs(readExternalDocs(node.get("externalDocs")));
        model.setOperationId(JsonUtil.stringProperty(node, "operationId"));
        model.setParameters(readParameterList(node.get("parameters")));
        model.setRequestBody(readRequestBody(node.get("requestBody")));
        model.setResponses(readAPIResponses(node.get("responses")));
        model.setCallbacks(readCallbacks(node.get("callbacks")));
        model.setDeprecated(JsonUtil.booleanProperty(node, "deprecated"));
        model.setSecurity(readSecurityRequirements(node.get("security")));
        model.setServers(readServers(node.get("servers")));
        readExtensions(node, model);
        return model;
    }

    /**
     * Reads a {@link APIResponses} OpenAPI node.
     * @param node
     */
    private APIResponses readAPIResponses(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        APIResponsesImpl model = new APIResponsesImpl();
        model.setDefaultValue(readAPIResponse(node.get("default")));
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            if ("default".equals(fieldName)) {
                continue;
            }
            model.addApiResponse(fieldName, readAPIResponse(node.get(fieldName)));
        }
        return model;
    }

    /**
     * Reads a list of {@link SecurityRequirement} OpenAPI nodes.
     * @param node
     */
    private List<SecurityRequirement> readSecurityRequirements(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<SecurityRequirement> model = new ArrayList<>(node.size());
        ArrayNode arrayNode = (ArrayNode) node;
        for (JsonNode arrayItem : arrayNode) {
            model.add(readSecurityRequirement(arrayItem));
        }
        return model;
    }

    /**
     * Reads a {@link APIResponses} OpenAPI node.
     * @param node
     */
    private SecurityRequirement readSecurityRequirement(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        SecurityRequirementImpl model = new SecurityRequirementImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            JsonNode scopesNode = node.get(fieldName);
            List<String> scopes = readStringArray(scopesNode);
            if (scopes == null) {
                model.addScheme(fieldName);
            } else {
                model.addScheme(fieldName, scopes);
            }
        }
        return model;
    }

    /**
     * Reads a {@link Parameter} OpenAPI node.
     * @param node
     */
    @SuppressWarnings("rawtypes")
    private List<Parameter> readParameterList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<Parameter> params = new ArrayList<>();
        ArrayNode arrayNode = (ArrayNode) node;
        for (JsonNode paramNode : arrayNode) {
            params.add(readParameter(paramNode));
        }
        return params;
    }

    /**
     * Reads a schema type.
     * @param node
     */
    private SchemaType readSchemaType(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        String strval = node.asText();
        return SchemaType.valueOf(strval.toUpperCase());
    }

    /**
     * Reads a string array.
     * @param node
     */
    private List<String> readStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<String> rval = new ArrayList<>(node.size());
        ArrayNode arrayNode = (ArrayNode) node;
        for (JsonNode arrayItem : arrayNode) {
            if (arrayItem != null) {
                rval.add(arrayItem.asText());
            }
        }
        return rval;
    }

    /**
     * Reads an object array.
     * @param node
     */
    private List<Object> readObjectArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<Object> rval = new ArrayList<>(node.size());
        ArrayNode arrayNode = (ArrayNode) node;
        for (JsonNode arrayItem : arrayNode) {
            if (arrayItem != null) {
                rval.add(readObject(arrayItem));
            }
        }
        return rval;
    }

    /**
     * Reads a list of schemas.
     * @param node
     */
    private List<Schema> readSchemaArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<Schema> rval = new ArrayList<>(node.size());
        ArrayNode arrayNode = (ArrayNode) node;
        for (JsonNode arrayItem : arrayNode) {
            rval.add(readSchema(arrayItem));
        }
        return rval;
    }

    /**
     * Reads a map of strings.
     * @param node
     */
    private Map<String, String> readStringMap(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, String> rval = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            String value = JsonUtil.stringProperty(node, fieldName);
            rval.put(fieldName, value);
        }
        return rval;
    }

    /**
     * Reads the node as a Java object.  This is typically expected to be a literal of
     * some sort, as in the case of default values and examples.  The node may be anything
     * from a string to a javascript object.
     * @param node
     */
    private Object readObject(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isBigDecimal()) {
            return new BigDecimal(node.asText());
        }
        if (node.isBigInteger()) {
            return new BigInteger(node.asText());
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isDouble()) {
            return node.asDouble();
        }
        if (node.isFloat()) {
            return node.asDouble();
        }
        if (node.isInt()) {
            return node.asInt();
        }
        if (node.isLong()) {
            return node.asLong();
        }
        if (node.isObject()) {
            return node;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return null;
    }

    /**
     * Reads model extensions.
     * @param node
     * @param model
     */
    private void readExtensions(JsonNode node, Extensible model) {
        for (Iterator<String> iterator = node.fieldNames(); iterator.hasNext();) {
            String fieldName = iterator.next();
            if (fieldName.toLowerCase().startsWith("x-")) {
                Object value = readObject(node.get(fieldName));
                model.addExtension(fieldName, value);
            }
        }
    }
}
