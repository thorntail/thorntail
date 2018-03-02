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

package org.wildfly.swarm.microprofile.openapi.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.wildfly.swarm.microprofile.openapi.api.models.ComponentsImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ExternalDocumentationImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.OperationImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.PathItemImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.PathsImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.callbacks.CallbackImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.examples.ExampleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.headers.HeaderImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.info.ContactImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.info.InfoImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.info.LicenseImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.links.LinkImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.media.ContentImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.media.DiscriminatorImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.media.EncodingImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.media.MediaTypeImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.media.SchemaImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.media.XMLImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.parameters.ParameterImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.parameters.RequestBodyImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.responses.APIResponseImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.responses.APIResponsesImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.security.OAuthFlowImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.security.OAuthFlowsImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.security.ScopesImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.security.SecurityRequirementImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.security.SecuritySchemeImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.servers.ServerImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.servers.ServerVariableImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.servers.ServerVariablesImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.tags.TagImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer.Format;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * A class used to parse an OpenAPI document (either YAML or JSON) into a Microprofile OpenAPI model tree.
 * @author eric.wittmann@gmail.com
 */
public class OpenApiParser {

    private static final Map<String, Style> ENCODING_STYLE_LOOKUP = new LinkedHashMap<>();
    private static final Map<String, org.eclipse.microprofile.openapi.models.parameters.Parameter.Style> PARAMETER_STYLE_LOOKUP = new LinkedHashMap<>();
    private static final Map<String, org.eclipse.microprofile.openapi.models.headers.Header.Style> HEADER_STYLE_LOOKUP = new LinkedHashMap<>();
    private static final Map<String, Type> SECURITY_SCHEME_TYPE_LOOKUP = new LinkedHashMap<>();
    private static final Map<String, In> SECURITY_SCHEME_IN_LOOKUP = new LinkedHashMap<>();
    private static final Map<String, org.eclipse.microprofile.openapi.models.parameters.Parameter.In> PARAMETER_IN_LOOKUP = new LinkedHashMap<>();

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

        org.eclipse.microprofile.openapi.models.parameters.Parameter.In[] parameterIns = org.eclipse.microprofile.openapi.models.parameters.Parameter.In.values();
        for (org.eclipse.microprofile.openapi.models.parameters.Parameter.In type : parameterIns) {
            PARAMETER_IN_LOOKUP.put(type.toString(), type);
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
            if (!isJson && !isYaml) {
                throw new IOException("Invalid file extension for URL (expected json, yaml, or yml): " + url.toURI().toString());
            }

            try (InputStream stream = url.openStream()) {
                return parse(stream, isJson ? Format.JSON : Format.YAML);
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    /**
     * Parses the resource found at the given stream.  The format of the stream must
     * be specified.
     * @param stream
     * @param format
     */
    public static final OpenAPIImpl parse(InputStream stream, Format format) throws IOException {
        ObjectMapper mapper;
        if (format == Format.JSON) {
            mapper = new ObjectMapper();
        } else {
            mapper = new ObjectMapper(new YAMLFactory());
        }
        JsonNode tree = mapper.readTree(stream);

        OpenApiParser parser = new OpenApiParser(tree);
        return parser.parse();
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
        model.setOpenapi(JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPENAPI));
        model.setInfo(readInfo(node.get(OpenApiConstants.PROP_INFO)));
        model.setExternalDocs(readExternalDocs(node.get(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        model.setServers(readServers(node.get(OpenApiConstants.PROP_SERVERS)));
        model.setSecurity(readSecurityRequirements(node.get(OpenApiConstants.PROP_SECURITY)));
        model.setTags(readTags(node.get(OpenApiConstants.PROP_TAGS)));
        model.setPaths(readPaths(node.get(OpenApiConstants.PROP_PATHS)));
        model.setComponents(readComponents(node.get(OpenApiConstants.PROP_COMPONENTS)));
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
        model.setTitle(JsonUtil.stringProperty(node, OpenApiConstants.PROP_TITLE));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setTermsOfService(JsonUtil.stringProperty(node, OpenApiConstants.PROP_TERMS_OF_SERVICE));
        model.setContact(readContact(node.get(OpenApiConstants.PROP_CONTACT)));
        model.setLicense(readLicense(node.get(OpenApiConstants.PROP_LICENSE)));
        model.setVersion(JsonUtil.stringProperty(node, OpenApiConstants.PROP_VERSION));
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
        model.setName(JsonUtil.stringProperty(node, OpenApiConstants.PROP_NAME));
        model.setUrl(JsonUtil.stringProperty(node, OpenApiConstants.PROP_URL));
        model.setEmail(JsonUtil.stringProperty(node, OpenApiConstants.PROP_EMAIL));
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
        model.setName(JsonUtil.stringProperty(node, OpenApiConstants.PROP_NAME));
        model.setUrl(JsonUtil.stringProperty(node, OpenApiConstants.PROP_URL));
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
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setUrl(JsonUtil.stringProperty(node, OpenApiConstants.PROP_URL));
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
            model.setName(JsonUtil.stringProperty(tagNode, OpenApiConstants.PROP_NAME));
            model.setDescription(JsonUtil.stringProperty(tagNode, OpenApiConstants.PROP_DESCRIPTION));
            model.setExternalDocs(readExternalDocs(tagNode.get(OpenApiConstants.PROP_EXTERNAL_DOCS)));
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
            model.setUrl(JsonUtil.stringProperty(serverNode, OpenApiConstants.PROP_URL));
            model.setDescription(JsonUtil.stringProperty(serverNode, OpenApiConstants.PROP_DESCRIPTION));
            model.setVariables(readServerVariables(serverNode.get(OpenApiConstants.PROP_VARIABLES)));
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
            if (!fieldName.toLowerCase().startsWith(OpenApiConstants.EXTENSION_PROPERTY_PREFIX)) {
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
        JsonNode enumNode = node.get(OpenApiConstants.PROP_ENUM);
        if (enumNode != null && enumNode.isArray()) {
            List<String> enums = new ArrayList<>(enumNode.size());
            for (JsonNode n : enumNode) {
                enums.add(n.asText());
            }
            model.setEnumeration(enums);
        }
        model.setDefaultValue(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DEFAULT));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
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
            if (fieldName.startsWith(OpenApiConstants.EXTENSION_PROPERTY_PREFIX)) {
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
        model.setSchemas(readSchemas(node.get(OpenApiConstants.PROP_SCHEMAS)));
        model.setResponses(readResponses(node.get(OpenApiConstants.PROP_RESPONSES)));
        model.setParameters(readParameters(node.get(OpenApiConstants.PROP_PARAMETERS)));
        model.setExamples(readExamples(node.get(OpenApiConstants.PROP_EXAMPLES)));
        model.setRequestBodies(readRequestBodies(node.get(OpenApiConstants.PROP_REQUEST_BODIES)));
        model.setHeaders(readHeaders(node.get(OpenApiConstants.PROP_HEADERS)));
        model.setSecuritySchemes(readSecuritySchemes(node.get(OpenApiConstants.PROP_SECURITY_SCHEMES)));
        model.setLinks(readLinks(node.get(OpenApiConstants.PROP_LINKS)));
        model.setCallbacks(readCallbacks(node.get(OpenApiConstants.PROP_CALLBACKS)));
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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        model.setFormat(JsonUtil.stringProperty(node, OpenApiConstants.PROP_FORMAT));
        model.setTitle(JsonUtil.stringProperty(node, OpenApiConstants.PROP_TITLE));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setDefaultValue(readObject(node.get(OpenApiConstants.PROP_DEFAULT)));
        model.setMultipleOf(JsonUtil.bigDecimalProperty(node, OpenApiConstants.PROP_MULTIPLE_OF));
        model.setMaximum(JsonUtil.bigDecimalProperty(node, OpenApiConstants.PROP_MAXIMUM));
        model.setExclusiveMaximum(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_EXCLUSIVE_MAXIMUM));
        model.setMinimum(JsonUtil.bigDecimalProperty(node, OpenApiConstants.PROP_MINIMUM));
        model.setExclusiveMinimum(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_EXCLUSIVE_MINIMUM));
        model.setMaxLength(JsonUtil.intProperty(node, OpenApiConstants.PROP_MAX_LENGTH));
        model.setMinLength(JsonUtil.intProperty(node, OpenApiConstants.PROP_MIN_LENGTH));
        model.setPattern(JsonUtil.stringProperty(node, OpenApiConstants.PROP_PATTERN));
        model.setMaxItems(JsonUtil.intProperty(node, OpenApiConstants.PROP_MAX_ITEMS));
        model.setMinItems(JsonUtil.intProperty(node, OpenApiConstants.PROP_MIN_ITEMS));
        model.setUniqueItems(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_UNIQUE_ITEMS));
        model.setMaxProperties(JsonUtil.intProperty(node, OpenApiConstants.PROP_MAX_PROPERTIES));
        model.setMinProperties(JsonUtil.intProperty(node, OpenApiConstants.PROP_MIN_PROPERTIES));
        model.setRequired(readStringArray(node.get(OpenApiConstants.PROP_REQUIRED)));
        model.setEnumeration(readObjectArray(node.get(OpenApiConstants.PROP_ENUM)));
        model.setType(readSchemaType(node.get(OpenApiConstants.PROP_TYPE)));
        model.setItems(readSchema(node.get(OpenApiConstants.PROP_ITEMS)));
        model.setNot(readSchema(node.get(OpenApiConstants.PROP_NOT)));
        model.setAllOf(readSchemaArray(node.get(OpenApiConstants.PROP_ALL_OF)));
        model.setProperties(readSchemas(node.get(OpenApiConstants.PROP_PROPERTIES)));
        if (node.has(OpenApiConstants.PROP_ADDITIONAL_PROPERTIES) && node.get(OpenApiConstants.PROP_ADDITIONAL_PROPERTIES).isObject()) {
            model.setAdditionalProperties(readSchema(node.get(OpenApiConstants.PROP_ADDITIONAL_PROPERTIES)));
        } else {
            model.setAdditionalProperties(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_ADDITIONAL_PROPERTIES));
        }
        model.setReadOnly(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_READ_ONLY));
        model.setXml(readXML(node.get(OpenApiConstants.PROP_XML)));
        model.setExternalDocs(readExternalDocs(node.get(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        model.setExample(readObject(node.get(OpenApiConstants.PROP_EXAMPLE)));
        model.setOneOf(readSchemaArray(node.get(OpenApiConstants.PROP_ONE_OF)));
        model.setAnyOf(readSchemaArray(node.get(OpenApiConstants.PROP_ANY_OF)));
        model.setNot(readSchema(node.get(OpenApiConstants.PROP_NOT)));
        model.setDiscriminator(readDiscriminator(node.get(OpenApiConstants.PROP_DISCRIMINATOR)));
        model.setNullable(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_NULLABLE));
        model.setWriteOnly(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_WRITE_ONLY));
        model.setDeprecated(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_DEPRECATED));
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
        model.setName(JsonUtil.stringProperty(node, OpenApiConstants.PROP_NAME));
        model.setNamespace(JsonUtil.stringProperty(node, OpenApiConstants.PROP_NAMESPACE));
        model.setPrefix(JsonUtil.stringProperty(node, OpenApiConstants.PROP_PREFIX));
        model.setAttribute(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_ATTRIBUTE));
        model.setWrapped(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_WRAPPED));
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
        model.setPropertyName(JsonUtil.stringProperty(node, OpenApiConstants.PROP_PROPERTY_NAME));
        model.setMapping(readStringMap(node.get(OpenApiConstants.PROP_MAPPING)));
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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setHeaders(readHeaders(node.get(OpenApiConstants.PROP_HEADERS)));
        model.setContent(readContent(node.get(OpenApiConstants.PROP_CONTENT)));
        model.setLinks(readLinks(node.get(OpenApiConstants.PROP_LINKS)));
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
        model.setSchema(readSchema(node.get(OpenApiConstants.PROP_SCHEMA)));
        model.setExample(readObject(node.get(OpenApiConstants.PROP_EXAMPLE)));
        model.setExamples(readExamples(node.get(OpenApiConstants.PROP_EXAMPLES)));
        model.setEncoding(readEncodings(node.get(OpenApiConstants.PROP_ENCODING)));
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
        model.setContentType(JsonUtil.stringProperty(node, OpenApiConstants.PROP_CONTENT_TYPE));
        model.setHeaders(readHeaders(node.get(OpenApiConstants.PROP_HEADERS)));
        model.setStyle(readEncodingStyle(node.get(OpenApiConstants.PROP_STYLE)));
        model.setExplode(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_EXPLODE));
        model.setAllowReserved(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_ALLOW_RESERVED));
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
    private Parameter readParameter(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Parameter model = new ParameterImpl();
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        model.setName(JsonUtil.stringProperty(node, OpenApiConstants.PROP_NAME));
        model.setIn(readParameterIn(node.get(OpenApiConstants.PROP_IN)));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setRequired(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_REQUIRED));
        model.setSchema(readSchema(node.get(OpenApiConstants.PROP_SCHEMA)));
        model.setAllowEmptyValue(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_ALLOW_EMPTY_VALUE));
        model.setDeprecated(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_DEPRECATED));
        model.setStyle(readParameterStyle(node.get(OpenApiConstants.PROP_STYLE)));
        model.setExplode(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_EXPLODE));
        model.setAllowReserved(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_ALLOW_RESERVED));
        model.setExample(readObject(node.get(OpenApiConstants.PROP_EXAMPLE)));
        model.setExamples(readExamples(node.get(OpenApiConstants.PROP_EXAMPLES)));
        model.setContent(readContent(node.get(OpenApiConstants.PROP_CONTENT)));
        readExtensions(node, model);

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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        model.setSummary(JsonUtil.stringProperty(node, OpenApiConstants.PROP_SUMMARY));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setValue(readObject(node.get(OpenApiConstants.PROP_VALUE)));
        model.setExternalValue(JsonUtil.stringProperty(node, OpenApiConstants.PROP_EXTERNAL_VALUE));
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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setContent(readContent(node.get(OpenApiConstants.PROP_CONTENT)));
        model.setRequired(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_REQUIRED));
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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setRequired(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_REQUIRED));
        model.setDeprecated(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_DEPRECATED));
        model.setAllowEmptyValue(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_ALLOW_EMPTY_VALUE));
        model.setStyle(readHeaderStyle(node.get(OpenApiConstants.PROP_STYLE)));
        model.setExplode(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_EXPLODE));
        model.setSchema(readSchema(node.get(OpenApiConstants.PROP_SCHEMA)));
        model.setExample(readObject(node.get(OpenApiConstants.PROP_EXAMPLE)));
        model.setExamples(readExamples(node.get(OpenApiConstants.PROP_EXAMPLES)));
        model.setContent(readContent(node.get(OpenApiConstants.PROP_CONTENT)));
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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        model.setType(readSecuritySchemeType(node.get(OpenApiConstants.PROP_TYPE)));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setName(JsonUtil.stringProperty(node, OpenApiConstants.PROP_NAME));
        model.setIn(readSecuritySchemeIn(node.get(OpenApiConstants.PROP_IN)));
        model.setScheme(JsonUtil.stringProperty(node, OpenApiConstants.PROP_SCHEME));
        model.setBearerFormat(JsonUtil.stringProperty(node, OpenApiConstants.PROP_BEARER_FORMAT));
        model.setFlows(readOAuthFlows(node.get(OpenApiConstants.PROP_FLOWS)));
        model.setOpenIdConnectUrl(JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPEN_ID_CONNECT_URL));
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
     * Reads a parameter 'in' property.
     * @param node
     */
    private org.eclipse.microprofile.openapi.models.parameters.Parameter.In readParameterIn(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return PARAMETER_IN_LOOKUP.get(node.asText());
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
        model.setImplicit(readOAuthFlow(node.get(OpenApiConstants.PROP_IMPLICIT)));
        model.setPassword(readOAuthFlow(node.get(OpenApiConstants.PROP_PASSWORD)));
        model.setClientCredentials(readOAuthFlow(node.get(OpenApiConstants.PROP_CLIENT_CREDENTIALS)));
        model.setAuthorizationCode(readOAuthFlow(node.get(OpenApiConstants.PROP_AUTHORIZATION_CODE)));
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
        model.setAuthorizationUrl(JsonUtil.stringProperty(node, OpenApiConstants.PROP_AUTHORIZATION_URL));
        model.setTokenUrl(JsonUtil.stringProperty(node, OpenApiConstants.PROP_TOKEN_URL));
        model.setRefreshUrl(JsonUtil.stringProperty(node, OpenApiConstants.PROP_REFRESH_URL));
        model.setScopes(readScopes(node.get(OpenApiConstants.PROP_SCOPES)));
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
            if (fieldName.startsWith(OpenApiConstants.EXTENSION_PROPERTY_PREFIX)) {
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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));

        model.setOperationRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_REF));
        model.setOperationId(JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_ID));
        model.setParameters(readLinkParameters(node.get(OpenApiConstants.PROP_PARAMETERS)));
        model.setRequestBody(readObject(node.get(OpenApiConstants.PROP_REQUEST_BODY)));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setServer(readServer(node.get(OpenApiConstants.PROP_SERVER)));
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
        model.setUrl(JsonUtil.stringProperty(node, OpenApiConstants.PROP_URL));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setVariables(readServerVariables(node.get(OpenApiConstants.PROP_VARIABLES)));
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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            if (fieldName.startsWith(OpenApiConstants.EXTENSION_PROPERTY_PREFIX) || fieldName.equals(OpenApiConstants.PROP_$REF)) {
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
        model.setRef(JsonUtil.stringProperty(node, OpenApiConstants.PROP_$REF));
        model.setSummary(JsonUtil.stringProperty(node, OpenApiConstants.PROP_SUMMARY));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setGET(readOperation(node.get(OpenApiConstants.PROP_GET)));
        model.setPUT(readOperation(node.get(OpenApiConstants.PROP_PUT)));
        model.setPOST(readOperation(node.get(OpenApiConstants.PROP_POST)));
        model.setDELETE(readOperation(node.get(OpenApiConstants.PROP_DELETE)));
        model.setOPTIONS(readOperation(node.get(OpenApiConstants.PROP_OPTIONS)));
        model.setHEAD(readOperation(node.get(OpenApiConstants.PROP_HEAD)));
        model.setPATCH(readOperation(node.get(OpenApiConstants.PROP_PATCH)));
        model.setTRACE(readOperation(node.get(OpenApiConstants.PROP_TRACE)));
        model.setParameters(readParameterList(node.get(OpenApiConstants.PROP_PARAMETERS)));
        model.setServers(readServers(node.get(OpenApiConstants.PROP_SERVERS)));
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
        model.setTags(readStringArray(node.get(OpenApiConstants.PROP_TAGS)));
        model.setSummary(JsonUtil.stringProperty(node, OpenApiConstants.PROP_SUMMARY));
        model.setDescription(JsonUtil.stringProperty(node, OpenApiConstants.PROP_DESCRIPTION));
        model.setExternalDocs(readExternalDocs(node.get(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        model.setOperationId(JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_ID));
        model.setParameters(readParameterList(node.get(OpenApiConstants.PROP_PARAMETERS)));
        model.setRequestBody(readRequestBody(node.get(OpenApiConstants.PROP_REQUEST_BODY)));
        model.setResponses(readAPIResponses(node.get(OpenApiConstants.PROP_RESPONSES)));
        model.setCallbacks(readCallbacks(node.get(OpenApiConstants.PROP_CALLBACKS)));
        model.setDeprecated(JsonUtil.booleanProperty(node, OpenApiConstants.PROP_DEPRECATED));
        model.setSecurity(readSecurityRequirements(node.get(OpenApiConstants.PROP_SECURITY)));
        model.setServers(readServers(node.get(OpenApiConstants.PROP_SERVERS)));
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
        model.setDefaultValue(readAPIResponse(node.get(OpenApiConstants.PROP_DEFAULT)));
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext(); ) {
            String fieldName = fieldNames.next();
            if (OpenApiConstants.PROP_DEFAULT.equals(fieldName)) {
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
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            List<Object> items = new ArrayList<>();
            for (JsonNode itemNode : arrayNode) {
                items.add(readObject(itemNode));
            }
            return items;
        }
        if (node.isObject()) {
            Map<String, Object> items = new LinkedHashMap<>();
            for (Iterator<Entry<String, JsonNode>> fields = node.fields(); fields.hasNext(); ) {
                Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                Object fieldValue = readObject(field.getValue());
                items.put(fieldName, fieldValue);
            }
            return items;
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
            if (fieldName.toLowerCase().startsWith(OpenApiConstants.EXTENSION_PROPERTY_PREFIX)) {
                Object value = readObject(node.get(fieldName));
                model.addExtension(fieldName, value);
            }
        }
    }
}
