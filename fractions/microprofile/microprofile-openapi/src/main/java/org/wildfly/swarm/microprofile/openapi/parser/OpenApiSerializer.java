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

package org.wildfly.swarm.microprofile.openapi.parser;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.microprofile.openapi.models.Extensible;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
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
//        private String openapi;
//        private Info info;
        writeExternalDocumentation(node, model.getExternalDocs());
        writeServers(node, model.getServers());
        writeSecurity(node, model.getSecurity());
        writeTags(node, model.getTags());
//        private Paths paths;
//        private Components components;
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
            JsonUtil.stringProperty(serverNode, "url", server.getUrl());
            JsonUtil.stringProperty(serverNode, "description", server.getDescription());
            writeServerVariables(serverNode, server.getVariables());
            writeExtensions(serverNode, server);
        }
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
    }

    /**
     * Writes the {@link SecurityRequirement} model array to the JSON tree.
     * @param node
     * @param security
     */
    private void writeSecurity(ObjectNode node, List<SecurityRequirement> security) {
        if (security == null) {
            return;
        }
        ArrayNode array = node.putArray("security");
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
            if (value instanceof String) {
                node.put(key, (String) value);
            } else if (value instanceof JsonNode) {
                node.set(key, (JsonNode) value);
            } else {
                // TOOD handle other data types!  lists, maps, numberics, booleans, etc
            }
        }
    }

}
