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
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.wildfly.swarm.microprofile.openapi.models.ExternalDocumentationImpl;
import org.wildfly.swarm.microprofile.openapi.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.models.info.ContactImpl;
import org.wildfly.swarm.microprofile.openapi.models.info.InfoImpl;
import org.wildfly.swarm.microprofile.openapi.models.info.LicenseImpl;
import org.wildfly.swarm.microprofile.openapi.models.security.SecurityRequirementImpl;
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
//        private String openapi;
//        private Info info;
        model.setExternalDocs(readExternalDocs(node.get("externalDocs")));
        model.setServers(readServers(node.get("servers")));
        model.setSecurity(readSecurity(node.get("security")));
        model.setTags(readTags(node.get("tags")));


        //        private List<Server> servers;
//        private List<SecurityRequirement> security;
//        private List<Tag> tags;
//        private Paths paths;
//        private Components components;
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
     * Reads a list of {@link SecurityRequirement} OpenAPI nodes.
     * @param node
     */
    private List<SecurityRequirement> readSecurity(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        ArrayNode nodes = (ArrayNode) node;
        List<SecurityRequirement> rval = new ArrayList<>(nodes.size());
        for (JsonNode securityRequirementNode : nodes) {
            SecurityRequirementImpl model = new SecurityRequirementImpl();
            for (Iterator<String> iter = securityRequirementNode.fieldNames(); iter.hasNext(); ) {
                String fieldName = iter.next();
                JsonNode valuesNode = securityRequirementNode.get(fieldName);
                if (valuesNode.isArray()) {
                    List<String> values = new ArrayList<>(valuesNode.size());
                    for (JsonNode valueNode : valuesNode) {
                        values.add(valueNode.asText());
                    }
                    model.put(fieldName, values);
                }
            }
            rval.add(model);
        }
        return rval;
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
                // TODO support more conversions than just String
                Object value;
                JsonNode extNode = node.get(fieldName);
                if (extNode != null) {
                    if (extNode.isTextual()) {
                        value = extNode.asText();
                    } else {
                        value = extNode;
                    }
                    model.addExtension(fieldName, value);
                }
            }
        }
    }

}
