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
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.wildfly.swarm.microprofile.openapi.models.OpenAPIImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * Class used to serialize an OpenAPI
 * @author eric.wittmann@gmail.com
 */
public class OpenApiSerializer {

    public static enum Format {
        JSON, YAML
    }

    /**
     * Serializes the given OpenAPI object into either JSON or YAML and returns it as a string.
     * @param oai
     * @param format
     */
    public static final String serialize(OpenAPIImpl oai, Format format) throws IOException {
        try {
            OpenApiSerializer serializer = new OpenApiSerializer(oai);
            JsonNode tree = serializer.serialize();

            ObjectMapper mapper;
            if (format == Format.JSON) {
                mapper = new ObjectMapper();
            } else {
                YAMLFactory factory = new YAMLFactory();
                factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
                mapper = new ObjectMapper(factory);
            }

            return mapper.writer().writeValueAsString(tree);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    private final OpenAPIImpl oai;

    /**
     * Constructor.
     * @param oai
     */
    public OpenApiSerializer(OpenAPIImpl oai) {
        this.oai = oai;
    }

    /**
     * Serializes the OAI model into a json/yaml tree.
     */
    private JsonNode serialize() {
        ObjectNode root = JsonUtil.objectNode();
        this.writeOpenAPI(root, this.oai);
        return root;
    }

    /**
     * Writes the given model.
     * @param node
     * @param model
     */
    private void writeOpenAPI(ObjectNode node, OpenAPI model) {
        JsonUtil.stringProperty(node, "openapi", model.getOpenapi());
        writeInfo(node, model.getInfo());
        writeExternalDocumentation(node, model.getExternalDocs());
        writeServers(node, model.getServers());
        writeSecurity(node, model.getSecurity());
        writeTags(node, model.getTags());
        writePaths(node, model.getPaths());
        writeComponents(node, model.getComponents());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link Info} model to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeInfo(ObjectNode parent, Info model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("info");

        JsonUtil.stringProperty(node, "title", model.getTitle());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        JsonUtil.stringProperty(node, "termsOfService", model.getTermsOfService());
        writeContact(node, model.getContact());
        writeLicense(node, model.getLicense());
        JsonUtil.stringProperty(node, "version", model.getVersion());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link Contact} model to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeContact(ObjectNode parent, Contact model) {
        if (model == null) {
            return;
        }
        ObjectNode node = JsonUtil.objectNode();
        parent.set("contact", node);

        JsonUtil.stringProperty(node, "name", model.getName());
        JsonUtil.stringProperty(node, "url", model.getUrl());
        JsonUtil.stringProperty(node, "email", model.getEmail());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link License} model to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeLicense(ObjectNode parent, License model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("license");

        JsonUtil.stringProperty(node, "name", model.getName());
        JsonUtil.stringProperty(node, "url", model.getUrl());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link ExternalDocumentation} model to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeExternalDocumentation(ObjectNode parent, ExternalDocumentation model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("externalDocs");

        JsonUtil.stringProperty(node, "description", model.getDescription());
        JsonUtil.stringProperty(node, "url", model.getUrl());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link Tag} model array to the JSON tree.
     * @param node
     * @param tags
     */
    private void writeTags(ObjectNode node, List<Tag> tags) {
        if (tags == null) {
            return;
        }
        ArrayNode array = node.putArray("tags");
        for (Tag tag : tags) {
            ObjectNode tagNode = array.addObject();
            JsonUtil.stringProperty(tagNode, "name", tag.getName());
            JsonUtil.stringProperty(tagNode, "description", tag.getDescription());
            writeExternalDocumentation(tagNode, tag.getExternalDocs());
            writeExtensions(tagNode, tag);
        }
    }

    /**
     * Writes the {@link Server} model array to the JSON tree.
     * @param node
     * @param servers
     */
    private void writeServers(ObjectNode node, List<Server> servers) {
        if (servers == null) {
            return;
        }
        ArrayNode array = node.putArray("servers");
        for (Server server : servers) {
            ObjectNode serverNode = array.addObject();
            writeServerToNode(serverNode, server);
        }
    }

    /**
     * Writes a {@link Server} model to the given JS node.
     * @param model
     * @param node
     */
    protected void writeServerToNode(ObjectNode node, Server model) {
        JsonUtil.stringProperty(node, "url", model.getUrl());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeServerVariables(node, model.getVariables());
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link ServerVariables} model to the JSON tree.
     * @param node
     * @param variables
     */
    private void writeServerVariables(ObjectNode serverNode, ServerVariables variables) {
        if (variables == null) {
            return;
        }
        ObjectNode variablesNode = serverNode.putObject("variables");
        for (String varName : variables.keySet()) {
            writeServerVariable(variablesNode, varName, variables.get(varName));
        }
        writeExtensions(variablesNode, variables);
    }

    /**
     * Writes a {@link ServerVariable} to the JSON tree.
     * @param parent
     * @param variableName
     * @param model
     */
    private void writeServerVariable(ObjectNode parent, String variableName, ServerVariable model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(variableName);
        JsonUtil.stringProperty(node, "default", model.getDefaultValue());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        List<String> enumeration = model.getEnumeration();
        if (enumeration != null) {
            ArrayNode enumArray = node.putArray("enum");
            for (String enumValue : enumeration) {
                enumArray.add(enumValue);
            }
        }
        writeExtensions(node, model);
    }

    /**
     * Writes the {@link SecurityRequirement} model array to the JSON tree.
     * @param parent
     * @param security
     */
    private void writeSecurity(ObjectNode parent, List<SecurityRequirement> security) {
        if (security == null) {
            return;
        }
        ArrayNode array = parent.putArray("security");
        for (SecurityRequirement securityRequirement : security) {
            ObjectNode srNode = array.addObject();
            for (String fieldName : securityRequirement.keySet()) {
                List<String> values = securityRequirement.get(fieldName);
                ArrayNode valuesNode = srNode.putArray(fieldName);
                for (String value : values) {
                    valuesNode.add(value);
                }
            }
        }
    }

    /**
     * Writes a {@link Paths} to the JSON tree.
     * @param parent
     * @param paths
     */
    private void writePaths(ObjectNode parent, Paths paths) {
        if (paths == null) {
            return;
        }
        ObjectNode pathsNode = parent.putObject("paths");
        for (String pathName : paths.keySet()) {
            writePathItem(pathsNode, paths.get(pathName), pathName);
        }
        writeExtensions(pathsNode, paths);
    }

    /**
     * Writes a {@link PathItem} to the JSON tree.
     * @param parent
     * @param model
     * @param pathName
     */
    private void writePathItem(ObjectNode parent, PathItem model, String pathName) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(pathName);
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "summary", model.getSummary());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeOperation(node, model.getGET(), "get");
        writeOperation(node, model.getPUT(), "put");
        writeOperation(node, model.getPOST(), "post");
        writeOperation(node, model.getDELETE(), "delete");
        writeOperation(node, model.getOPTIONS(), "options");
        writeOperation(node, model.getHEAD(), "head");
        writeOperation(node, model.getPATCH(), "patch");
        writeOperation(node, model.getTRACE(), "trace");
        writeParameterList(node, model.getParameters());
        writeServers(node, model.getServers());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link Operation} to the JSON tree.
     * @param parent
     * @param model
     * @param method
     */
    private void writeOperation(ObjectNode parent, Operation model, String method) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(method);
        writeStringArray(node, model.getTags(), "tags");
        JsonUtil.stringProperty(node, "summary", model.getSummary());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeExternalDocumentation(node, model.getExternalDocs());
        JsonUtil.stringProperty(node, "operationId", model.getOperationId());
        writeParameterList(node, model.getParameters());
        writeRequestBody(node, model.getRequestBody());
        writeAPIResponses(node, model.getResponses());
        writeCallbacks(node, model.getCallbacks());
        JsonUtil.booleanProperty(node, "deprecated", model.getDeprecated());
        writeSecurityRequirements(node, model.getSecurity());
        writeServers(node, model.getServers());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link RequestBody} to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeRequestBody(ObjectNode parent, RequestBody model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("requestBody");
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeContent(node, model.getContent());
        JsonUtil.booleanProperty(node, "required", model.getRequired());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link Content} to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeContent(ObjectNode parent, Content model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("content");
        for (String name : model.keySet()) {
            writeMediaType(node, model.get(name), name);
        }
    }

    /**
     * Writes a {@link MediaType} to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeMediaType(ObjectNode parent, MediaType model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        writeSchema(node, model.getSchema(), "schema");
        writeObject(node, "example", model.getExample());
        writeExamples(node, model.getExamples());
        writeEncodings(node, model.getEncoding());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link Schema} to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeSchema(ObjectNode parent, Schema model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        writeSchemaToNode(node, model);
    }

    /**
     * Writes the {@link Schema} model to the give node.
     * @param node
     * @param model
     */
    private void writeSchemaToNode(ObjectNode node, Schema model) {
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "format", model.getFormat());
        JsonUtil.stringProperty(node, "title", model.getTitle());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeObject(node, "default", model.getDefaultValue());
        JsonUtil.bigDecimalProperty(node, "multipleOf", model.getMultipleOf());
        JsonUtil.bigDecimalProperty(node, "maximum", model.getMaximum());
        JsonUtil.booleanProperty(node, "exclusiveMaximum", model.getExclusiveMaximum());
        JsonUtil.bigDecimalProperty(node, "minimum", model.getMinimum());
        JsonUtil.booleanProperty(node, "exclusiveMinimum", model.getExclusiveMinimum());
        JsonUtil.intProperty(node, "maxLength", model.getMaxLength());
        JsonUtil.intProperty(node, "minLength", model.getMinLength());
        JsonUtil.stringProperty(node, "pattern", model.getPattern());
        JsonUtil.intProperty(node, "maxItems", model.getMaxItems());
        JsonUtil.intProperty(node, "minItems", model.getMinItems());
        JsonUtil.booleanProperty(node, "uniqueItems", model.getUniqueItems());
        JsonUtil.intProperty(node, "maxProperties", model.getMaxProperties());
        JsonUtil.intProperty(node, "minProperties", model.getMinProperties());
        writeStringArray(node, model.getRequired(), "required");
//        private List<Object> enumeration"
        JsonUtil.enumProperty(node, "type", model.getType());
        writeSchema(node, model.getItems(), "items");
        writeSchemaList(node, model.getAllOf(), "allOf");
        writeSchemas(node, model.getProperties(), "properties");
        if (model.getAdditionalProperties() instanceof Boolean) {
            JsonUtil.booleanProperty(node, "additionalProperties", (Boolean) model.getAdditionalProperties());
        } else {
            writeSchema(node, (Schema) model.getAdditionalProperties(), "additionalProperties");
        }
        JsonUtil.booleanProperty(node, "readOnly", model.getReadOnly());
        writeXML(node, model.getXml());
        writeExternalDocumentation(node, model.getExternalDocs());
        writeObject(node, "example", model.getExample());
        writeSchemaList(node, model.getOneOf(), "oneOf");
        writeSchemaList(node, model.getAnyOf(), "anyOf");
        writeSchema(node, model.getNot(), "not");
        writeDiscriminator(node, model.getDiscriminator());
        JsonUtil.booleanProperty(node, "nullable", model.getNullable());
        JsonUtil.booleanProperty(node, "writeOnly", model.getWriteOnly());
        JsonUtil.booleanProperty(node, "deprecated", model.getDeprecated());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link XML} object to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeXML(ObjectNode parent, XML model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("xml");
        JsonUtil.stringProperty(node, "name", model.getName());
        JsonUtil.stringProperty(node, "namespace", model.getNamespace());
        JsonUtil.stringProperty(node, "prefix", model.getPrefix());
        JsonUtil.booleanProperty(node, "attribute", model.getAttribute());
        JsonUtil.booleanProperty(node, "wrapped", model.getWrapped());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link Discriminator} object to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeDiscriminator(ObjectNode parent, Discriminator model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("discriminator");
        JsonUtil.stringProperty(node, "propertyName", model.getPropertyName());
        writeStringMap(node, model.getMapping(), "mapping");
    }

    /**
     * Writes a map of {@link Encoding} objects to the JSON tree.
     * @param parent
     * @param models
     */
    private void writeEncodings(ObjectNode parent, Map<String, Encoding> models) {
        if (models == null) {
            return;
        }
        ObjectNode node = parent.putObject("encoding");
        for (String name : models.keySet()) {
            Encoding encoding = models.get(name);
            writeEncoding(node, encoding, name);
        }
    }

    /**
     * Writes a {@link Encoding} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeEncoding(ObjectNode parent, Encoding model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "contentType", model.getContentType());
        writeHeaders(node, model.getHeaders());
        JsonUtil.enumProperty(node, "style", model.getStyle());
        JsonUtil.booleanProperty(node, "explode", model.getExplode());
        JsonUtil.booleanProperty(node, "allowReserved", model.getAllowReserved());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link APIResponses} map to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeAPIResponses(ObjectNode parent, APIResponses model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("responses");
        for (String name : model.keySet()) {
            writeAPIResponse(node, model.get(name), name);
        }
    }

    /**
     * Writes a {@link APIResponse} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeAPIResponse(ObjectNode parent, APIResponse model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeHeaders(node, model.getHeaders());
        writeContent(node, model.getContent());
        writeLinks(node, model.getLinks());
        writeExtensions(node, model);
    }

    /**
     * Writes a list of {@link SecurityRequirement} to the JSON tree.
     * @param parent
     * @param models
     */
    private void writeSecurityRequirements(ObjectNode parent, List<SecurityRequirement> models) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray("security");
        for (SecurityRequirement securityRequirement : models) {
            ObjectNode secNode = node.objectNode();
            writeSecurityRequirementToNode(secNode, securityRequirement);
        }
    }

    /**
     * Writes a {@link SecurityRequirement} to the given JS node.
     * @param node
     * @param model
     */
    private void writeSecurityRequirementToNode(ObjectNode node, SecurityRequirement model) {
        if (model == null) {
            return;
        }
        for (String name : model.keySet()) {
            List<String> scopes = model.get(name);
            writeStringArray(node, scopes, name);
        }
    }

    /**
     * Writes a list of {@link Parameter} to the JSON tree.
     * @param parent
     * @param models
     */
    @SuppressWarnings("rawtypes")
    private void writeParameterList(ObjectNode parent, List<Parameter> models) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray("parameters");
        for (Parameter model : models) {
            ObjectNode paramNode = node.addObject();
            writeParameterToNode(paramNode, model);
        }
    }

    /**
     * Writes a {@link Parameter} into the JSON node.
     * @param node
     * @param model
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void writeParameterToNode(ObjectNode node, Parameter model) {
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "name", model.getName());
        JsonUtil.enumProperty(node, "in", model.getIn());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        JsonUtil.booleanProperty(node, "required", model.getRequired());
        writeSchema(node, model.getSchema(), "schema");
        JsonUtil.booleanProperty(node, "allowEmptyValue", model.getAllowEmptyValue());
        JsonUtil.booleanProperty(node, "deprecated", model.getDeprecated());
        JsonUtil.enumProperty(node, "style", model.getStyle());
        JsonUtil.booleanProperty(node, "explode", model.getExplode());
        JsonUtil.booleanProperty(node, "allowReserved", model.getAllowReserved());
        writeObject(node, "example", model.getExample());
        writeExamples(node, model.getExamples());
        writeContent(node, model.getContent());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link ServerVariable} to the JSON tree.
     * @param parent
     * @param components
     */
    private void writeComponents(ObjectNode parent, Components components) {
        if (components == null) {
            return;
        }
        ObjectNode node = parent.putObject("components");
        writeSchemas(node, components.getSchemas());
        writeResponses(node, components.getResponses());
        writeParameters(node, components.getParameters());
        writeExamples(node, components.getExamples());
        writeRequestBodies(node, components.getRequestBodies());
        writeHeaders(node, components.getHeaders());
        writeSecuritySchemes(node, components.getSecuritySchemes());
        writeLinks(node, components.getLinks());
        writeCallbacks(node, components.getCallbacks());
        writeExtensions(node, components);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param schemas
     */
    private void writeSchemas(ObjectNode parent, Map<String, Schema> schemas) {
        writeSchemas(parent, schemas, "schemas");
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param schemas
     */
    private void writeSchemas(ObjectNode parent, Map<String, Schema> schemas, String propertyName) {
        if (schemas == null) {
            return;
        }
        ObjectNode schemasNode = parent.putObject(propertyName);
        for (String schemaName : schemas.keySet()) {
            writeSchema(schemasNode, schemas.get(schemaName), schemaName);
        }
    }

    /**
     * Writes a list of {@link Schema} to the JSON tree.
     * @param parent
     * @param models
     * @param propertyName
     */
    private void writeSchemaList(ObjectNode parent, List<Schema> models, String propertyName) {
        if (models == null) {
            return;
        }
        ArrayNode schemasNode = parent.putArray(propertyName);
        for (Schema schema : models) {
            writeSchemaToNode(schemasNode.objectNode(), schema);
        }
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param responses
     */
    private void writeResponses(ObjectNode parent, Map<String, APIResponse> responses) {
        if (responses == null) {
            return;
        }
        ObjectNode responsesNode = parent.putObject("responses");
        for (String responseName : responses.keySet()) {
            writeAPIResponse(responsesNode, responses.get(responseName), responseName);
        }
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param parameters
     */
    @SuppressWarnings("rawtypes")
    private void writeParameters(ObjectNode parent, Map<String, Parameter> parameters) {
        if (parameters == null) {
            return;
        }
        ObjectNode parametersNode = parent.putObject("parameters");
        for (String parameterName : parameters.keySet()) {
            writeParameter(parametersNode, parameters.get(parameterName), parameterName);
        }
    }

    /**
     * Writes a {@link Parameter} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    @SuppressWarnings("rawtypes")
    private void writeParameter(ObjectNode parent, Parameter model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        writeParameterToNode(node, model);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param examples
     */
    private void writeExamples(ObjectNode parent, Map<String, Example> examples) {
        if (examples == null) {
            return;
        }
        ObjectNode examplesNode = parent.putObject("examples");
        for (String exampleName : examples.keySet()) {
            writeExample(examplesNode, examples.get(exampleName), exampleName);
        }
    }

    /**
     * Writes a {@link Example} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeExample(ObjectNode parent, Example model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "summary", model.getSummary());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeObject(node, "value", model.getValue());
        JsonUtil.stringProperty(node, "externalValue", model.getExternalValue());
        writeExtensions(node, model);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param requestBodies
     */
    private void writeRequestBodies(ObjectNode parent, Map<String, RequestBody> requestBodies) {
        if (requestBodies == null) {
            return;
        }
        ObjectNode requestBodiesNode = parent.putObject("requestBodies");
        for (String requestBodyName : requestBodies.keySet()) {
            writeRequestBody(requestBodiesNode, requestBodies.get(requestBodyName), requestBodyName);
        }
    }

    /**
     * Writes a {@link RequestBody} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeRequestBody(ObjectNode parent, RequestBody model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeContent(node, model.getContent());
        JsonUtil.booleanProperty(node, "required", model.getRequired());
        writeExtensions(node, model);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param headers
     */
    private void writeHeaders(ObjectNode parent, Map<String, Header> headers) {
        if (headers == null) {
            return;
        }
        ObjectNode headersNode = parent.putObject("headers");
        for (String headerName : headers.keySet()) {
            writeHeader(headersNode, headers.get(headerName), headerName);
        }
    }

    /**
     * Writes a {@link RequestBody} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeHeader(ObjectNode parent, Header model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        JsonUtil.booleanProperty(node, "required", model.getRequired());
        JsonUtil.booleanProperty(node, "deprecated", model.getDeprecated());
        JsonUtil.booleanProperty(node, "allowEmptyValue", model.getAllowEmptyValue());
        JsonUtil.enumProperty(node, "style", model.getStyle());
        JsonUtil.booleanProperty(node, "explode", model.getExplode());
        writeSchema(node, model.getSchema(), "schema");
        writeObject(node, "example", model.getExample());
        writeExamples(node, model.getExamples());
        writeContent(node, model.getContent());
        writeExtensions(node, model);

    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param securitySchemes
     */
    private void writeSecuritySchemes(ObjectNode parent, Map<String, SecurityScheme> securitySchemes) {
        if (securitySchemes == null) {
            return;
        }
        ObjectNode securitySchemesNode = parent.putObject("securitySchemes");
        for (String securitySchemeName : securitySchemes.keySet()) {
            writeSecurityScheme(securitySchemesNode, securitySchemes.get(securitySchemeName), securitySchemeName);
        }
    }

    /**
     * Writes a {@link SecurityScheme} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeSecurityScheme(ObjectNode parent, SecurityScheme model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.enumProperty(node, "type", model.getType());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        JsonUtil.stringProperty(node, "name", model.getName());
        JsonUtil.enumProperty(node, "in", model.getIn());
        JsonUtil.stringProperty(node, "scheme", model.getScheme());
        JsonUtil.stringProperty(node, "bearerFormat", model.getBearerFormat());
        writeOAuthFlows(node, model.getFlows());
        JsonUtil.stringProperty(node, "openIdConnectUrl", model.getOpenIdConnectUrl());
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link OAuthFlows} object to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeOAuthFlows(ObjectNode parent, OAuthFlows model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("flows");
        writeOAuthFlow(node, model.getImplicit(), "implicit");
        writeOAuthFlow(node, model.getPassword(), "password");
        writeOAuthFlow(node, model.getClientCredentials(), "clientCredentials");
        writeOAuthFlow(node, model.getAuthorizationCode(), "authorizationCode");
        writeExtensions(node, model);
    }

    /**
     * Writes a {@link OAuthFlow} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeOAuthFlow(ObjectNode parent, OAuthFlow model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "authorizationUrl", model.getAuthorizationUrl());
        JsonUtil.stringProperty(node, "tokenUrl", model.getTokenUrl());
        JsonUtil.stringProperty(node, "refreshUrl", model.getRefreshUrl());
        writeStringMap(node, model.getScopes(), "scopes");
        writeExtensions(node, model);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param links
     */
    private void writeLinks(ObjectNode parent, Map<String, Link> links) {
        if (links == null) {
            return;
        }
        ObjectNode linksNode = parent.putObject("links");
        for (String linkName : links.keySet()) {
            writeLink(linksNode, links.get(linkName), linkName);
        }
    }

    /**
     * Writes a {@link Link} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeLink(ObjectNode parent, Link model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        JsonUtil.stringProperty(node, "operationRef", model.getOperationRef());
        JsonUtil.stringProperty(node, "operationId", model.getOperationId());
        writeLinkParameters(node, model.getParameters());
        writeObject(node, "requestBody", model.getRequestBody());
        JsonUtil.stringProperty(node, "description", model.getDescription());
        writeServer(node, model.getServer());
        writeExtensions(node, model);
    }

    /**
     * Writes the link parameters to the given node.
     * @param parent
     * @param parameters
     */
    private void writeLinkParameters(ObjectNode parent, Map<String, Object> parameters) {
        if (parameters == null) {
            return;
        }
        ObjectNode node = parent.putObject("parameters");
        for (String name : parameters.keySet()) {
            writeObject(node, name, parameters.get(name));
        }
    }

    /**
     * Writes a {@link Server} object to the JSON tree.
     * @param parent
     * @param model
     */
    private void writeServer(ObjectNode parent, Server model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject("server");
        writeServerToNode(node, model);
    }

    /**
     * Writes a map of {@link Schema} to the JSON tree.
     * @param parent
     * @param callbacks
     */
    private void writeCallbacks(ObjectNode parent, Map<String, Callback> callbacks) {
        if (callbacks == null) {
            return;
        }
        ObjectNode callbacksNode = parent.putObject("callbacks");
        for (String callbackName : callbacks.keySet()) {
            writeCallback(callbacksNode, callbacks.get(callbackName), callbackName);
        }
    }

    /**
     * Writes a {@link Callback} object to the JSON tree.
     * @param parent
     * @param model
     * @param name
     */
    private void writeCallback(ObjectNode parent, Callback model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, "$ref", model.getRef());
        for (String pathItemName : model.keySet()) {
            writePathItem(node, model.get(pathItemName), pathItemName);
        }
        writeExtensions(node, model);
    }

    /**
     * Writes extensions to the JSON tree.
     * @param node
     * @param model
     */
    private void writeExtensions(ObjectNode node, Extensible model) {
        Map<String, Object> extensions = model.getExtensions();
        if (extensions == null || extensions.isEmpty()) {
            return;
        }
        for (Entry<String, Object> entry : extensions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            writeObject(node, key, value);
        }
    }

    /**
     * Writes an array of strings to the parent node.
     * @param parent
     * @param models
     * @param propertyName
     */
    private void writeStringArray(ObjectNode parent, List<String> models, String propertyName) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(propertyName);
        for (String model : models) {
            node.add(model);
        }
    }

    /**
     * Writes a map of strings to the parent node.
     * @param parent
     * @param models
     * @param propertyName
     */
    private void writeStringMap(ObjectNode parent, Map<String, String> models, String propertyName) {
        if (models == null) {
            return;
        }
        ObjectNode node = parent.putObject(propertyName);
        for (String name : models.keySet()) {
            String value = models.get(name);
            node.put(name, value);
        }
    }

    /**
     * @param node
     * @param key
     * @param value
     */
    protected void writeObject(ObjectNode node, String key, Object value) {
        if (value instanceof String) {
            node.put(key, (String) value);
        } else if (value instanceof JsonNode) {
            node.set(key, (JsonNode) value);
        } else if (value instanceof BigDecimal) {
            node.put(key, (BigDecimal) value);
        } else if (value instanceof BigInteger) {
            node.put(key, ((BigInteger) value).longValue());
        } else if (value instanceof Boolean) {
            node.put(key, (Boolean) value);
        } else if (value instanceof Double) {
            node.put(key, (Double) value);
        } else if (value instanceof Float) {
            node.put(key, (Float) value);
        } else if (value instanceof Integer) {
            node.put(key, (Integer) value);
        } else if (value instanceof Long) {
            node.put(key, (Long) value);
        } else {
            node.put(key, (String) null);
        }
    }

}
