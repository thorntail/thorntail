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

package org.wildfly.swarm.microprofile.openapi.runtime;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
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
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.microprofile.openapi.io.ModelConstants;
import org.wildfly.swarm.microprofile.openapi.models.ComponentsImpl;
import org.wildfly.swarm.microprofile.openapi.models.ExternalDocumentationImpl;
import org.wildfly.swarm.microprofile.openapi.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.models.OperationImpl;
import org.wildfly.swarm.microprofile.openapi.models.PathItemImpl;
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
import org.wildfly.swarm.microprofile.openapi.models.parameters.ParameterImpl;
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
import org.wildfly.swarm.microprofile.openapi.util.AnnotationUtil;

/**
 * Scans a deployment (using the archive and jandex annotation index) for JAX-RS and
 * OpenAPI annotations.  These annotations, if found, are used to generate a valid
 * OpenAPI model.  For reference, see:
 *
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#annotations
 *
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
public class OpenApiAnnotationScanner {

    private static final String PROP_REF = "ref";
    private static final String PROP_METHOD = "method";
    private static final String PROP_CALLBACK_URL_EXPRESSION = "callbackUrlExpression";
    private static final String PROP_OPERATIONS = "operations";
    private static final String PROP_EXTENSIONS = "extensions";
    private static final String PROP_EXPRESSION = "expression";
    private static final String PROP_HIDDEN = "hidden";
    private static final String PROP_MEDIA_TYPE = "mediaType";
    private static final String PROP_REQUIRED_PROPERTIES = "requiredProperties";
    private static final String PROP_DEFAULT_VALUE = "defaultValue";
    private static final String PROP_DISCRIMINATOR_MAPPING = "discriminatorMapping";
    private static final String PROP_SECURITY_SCHEME_NAME = "securitySchemeName";

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.openapi");

    private static final String OPEN_API_VERSION = "3.0.0";

    @SuppressWarnings("unused")
    private final OpenApiConfig config;
    @SuppressWarnings("unused")
    private final Archive archive;
    private final IndexView index;

    /**
     * Constructor.
     * @param config
     * @param archive
     * @param index
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, Archive archive, IndexView index) {
        this.config = config;
        this.archive = archive;
        this.index = index;
    }

    /**
     * Scan the deployment for relevant annotations.  Returns an OpenAPI data model that was
     * built from those found annotations.
     */
    public OpenAPIImpl scan() {
        LOG.debug("Scanning deployment for OpenAPI and JAX-RS Annotations.");
        OpenAPIImpl openApi = new OpenAPIImpl();
        openApi.setOpenapi(OPEN_API_VERSION);

        Collection<AnnotationInstance> definitions = this.index.getAnnotations(DotName.createSimple(OpenAPIDefinition.class.getName()));
        // TODO what to do if multiple @OpenAPIDefinition annotations are found?  Throw error?
        if (!definitions.isEmpty()) {
            AnnotationInstance definitionAnno = definitions.iterator().next();
            processDefinition(openApi, definitionAnno);
        }

        return openApi;
    }

    /**
     * Reads a OpenAPIDefinition annotation.
     * @param openApi
     * @param definitionAnno
     */
    protected void processDefinition(OpenAPIImpl openApi, AnnotationInstance definitionAnno) {
        LOG.debug("Processing an @OpenAPIDefinition annotation.");
        openApi.setInfo(readInfo(definitionAnno.value(ModelConstants.PROP_INFO)));
        openApi.setTags(readTags(definitionAnno.value(ModelConstants.PROP_TAGS)));
        openApi.setServers(readServers(definitionAnno.value(ModelConstants.PROP_SERVERS)));
        openApi.setSecurity(readSecurity(definitionAnno.value(ModelConstants.PROP_SECURITY)));
        openApi.setExternalDocs(readExternalDocs(definitionAnno.value(ModelConstants.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(readComponents(definitionAnno.value(ModelConstants.PROP_COMPONENTS)));
    }

    /**
     * Reads an Info annotation.
     * @param infoAnno
     */
    private Info readInfo(AnnotationValue infoAnno) {
        if (infoAnno == null) {
            return null;
        }
        LOG.debug("Processing an @Info annotation.");
        AnnotationInstance nested = infoAnno.asNested();
        InfoImpl info = new InfoImpl();
        info.setTitle(AnnotationUtil.stringValue(nested, ModelConstants.PROP_TITLE));
        info.setDescription(AnnotationUtil.stringValue(nested, ModelConstants.PROP_DESCRIPTION));
        info.setTermsOfService(AnnotationUtil.stringValue(nested, ModelConstants.PROP_TERMS_OF_SERVICE));
        info.setContact(readContact(nested.value(ModelConstants.PROP_CONTACT)));
        info.setLicense(readLicense(nested.value(ModelConstants.PROP_LICENSE)));
        info.setVersion(AnnotationUtil.stringValue(nested, ModelConstants.PROP_VERSION));
        return info;
    }

    /**
     * Reads an Contact annotation.
     * @param contactAnno
     */
    private Contact readContact(AnnotationValue contactAnno) {
        if (contactAnno == null) {
            return null;
        }
        LOG.debug("Processing an @Contact annotation.");
        AnnotationInstance nested = contactAnno.asNested();
        ContactImpl contact = new ContactImpl();
        contact.setName(AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME));
        contact.setUrl(AnnotationUtil.stringValue(nested, ModelConstants.PROP_URL));
        contact.setEmail(AnnotationUtil.stringValue(nested, ModelConstants.PROP_EMAIL));
        return contact;
    }

    /**
     * Reads an License annotation.
     * @param licenseAnno
     */
    private License readLicense(AnnotationValue licenseAnno) {
        if (licenseAnno == null) {
            return null;
        }
        LOG.debug("Processing an @License annotation.");
        AnnotationInstance nested = licenseAnno.asNested();
        LicenseImpl license = new LicenseImpl();
        license.setName(AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME));
        license.setUrl(AnnotationUtil.stringValue(nested, ModelConstants.PROP_URL));
        return license;
    }

    /**
     * Reads any Tag annotations.  The annotation
     * value is an array of Tag annotations.
     * @param tagAnnos
     */
    private List<Tag> readTags(AnnotationValue tagAnnos) {
        if (tagAnnos == null) {
            return null;
        }
        LOG.debug("Processing an array of @Tag annotations.");
        AnnotationInstance[] nestedArray = tagAnnos.asNestedArray();
        List<Tag> tags = new ArrayList<>();
        for (AnnotationInstance tagAnno : nestedArray) {
            tags.add(readTag(tagAnno));
        }
        return tags;
    }

    /**
     * Reads a single Tag annotation.
     * @param tagAnno
     */
    private Tag readTag(AnnotationInstance tagAnno) {
        if (tagAnno == null) {
            return null;
        }
        LOG.debug("Processing a single @Tag annotation.");
        TagImpl tag = new TagImpl();
        tag.setName(AnnotationUtil.stringValue(tagAnno, ModelConstants.PROP_NAME));
        tag.setDescription(AnnotationUtil.stringValue(tagAnno, ModelConstants.PROP_DESCRIPTION));
        tag.setExternalDocs(readExternalDocs(tagAnno.value(ModelConstants.PROP_EXTERNAL_DOCS)));
        return tag;
    }

    /**
     * Reads any Server annotations.  The annotation value is an array of Server annotations.
     * @param serverAnnos
     */
    private List<Server> readServers(AnnotationValue serverAnnos) {
        if (serverAnnos == null) {
            return null;
        }
        LOG.debug("Processing an array of @Server annotations.");
        AnnotationInstance[] nestedArray = serverAnnos.asNestedArray();
        List<Server> servers = new ArrayList<>();
        for (AnnotationInstance serverAnno : nestedArray) {
            servers.add(readServer(serverAnno));
        }
        return servers;
    }

    /**
     * Reads a single Server annotation.
     * @param serverAnno
     */
    private Server readServer(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readServer(value.asNested());
    }

    /**
     * Reads a single Server annotation.
     * @param serverAnno
     */
    private Server readServer(AnnotationInstance serverAnno) {
        if (serverAnno == null) {
            return null;
        }
        LOG.debug("Processing a single @Server annotation.");
        ServerImpl server = new ServerImpl();
        server.setUrl(AnnotationUtil.stringValue(serverAnno, ModelConstants.PROP_URL));
        server.setDescription(AnnotationUtil.stringValue(serverAnno, ModelConstants.PROP_DESCRIPTION));
        server.setVariables(readServerVariables(serverAnno.value(ModelConstants.PROP_VARIABLES)));
        return server;
    }

    /**
     * Reads an array of ServerVariable annotations, returning a new {@link ServerVariables} model.  The
     * annotation value is an array of ServerVariable annotations.
     * @param value
     * @return
     */
    private ServerVariables readServerVariables(AnnotationValue serverVariableAnnos) {
        if (serverVariableAnnos == null) {
            return null;
        }
        LOG.debug("Processing an array of @ServerVariable annotations.");
        AnnotationInstance[] nestedArray = serverVariableAnnos.asNestedArray();
        ServerVariables variables = new ServerVariablesImpl();
        for (AnnotationInstance serverVariableAnno : nestedArray) {
            String name = AnnotationUtil.stringValue(serverVariableAnno, ModelConstants.PROP_NAME);
            if (name != null) {
                variables.addServerVariable(name, readServerVariable(serverVariableAnno));
            }
        }
        return variables;
    }

    /**
     * Reads a single ServerVariable annotation.
     * @param serverVariableAnno
     */
    private ServerVariable readServerVariable(AnnotationInstance serverVariableAnno) {
        if (serverVariableAnno == null) {
            return null;
        }
        LOG.debug("Processing a single @ServerVariable annotation.");
        ServerVariable variable = new ServerVariableImpl();
        variable.setDescription(AnnotationUtil.stringValue(serverVariableAnno, ModelConstants.PROP_DESCRIPTION));
        variable.setEnumeration(AnnotationUtil.stringListValue(serverVariableAnno, ModelConstants.PROP_ENUM));
        variable.setDefaultValue(AnnotationUtil.stringValue(serverVariableAnno, ModelConstants.PROP_DEFAULT));
        return variable;
    }

    /**
     * Reads any SecurityRequirement annotations.  The annotation value is an array of
     * SecurityRequirement annotations.
     * @param value
     */
    private List<SecurityRequirement> readSecurity(AnnotationValue securityRequirementAnnos) {
        if (securityRequirementAnnos == null) {
            return null;
        }
        LOG.debug("Processing an array of @SecurityRequirement annotations.");
        AnnotationInstance[] nestedArray = securityRequirementAnnos.asNestedArray();
        List<SecurityRequirement> requirements = new ArrayList<>();
        for (AnnotationInstance requirementAnno : nestedArray) {
            String name = AnnotationUtil.stringValue(requirementAnno, ModelConstants.PROP_NAME);
            if (name != null) {
                List<String> scopes = AnnotationUtil.stringListValue(requirementAnno, ModelConstants.PROP_SCOPES);
                SecurityRequirement requirement = new SecurityRequirementImpl();
                if (scopes == null) {
                    requirement.addScheme(name);
                } else {
                    requirement.addScheme(name, scopes);
                }
                requirements.add(requirement);
            }
        }
        return requirements;
    }

    /**
     * Reads an ExternalDocumentation annotation.
     * @param externalDocAnno
     */
    private ExternalDocumentation readExternalDocs(AnnotationValue externalDocAnno) {
        if (externalDocAnno == null) {
            return null;
        }
        LOG.debug("Processing an @ExternalDocumentation annotation.");
        AnnotationInstance nested = externalDocAnno.asNested();
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(AnnotationUtil.stringValue(nested, ModelConstants.PROP_DESCRIPTION));
        externalDoc.setUrl(AnnotationUtil.stringValue(nested, ModelConstants.PROP_URL));
        return externalDoc;
    }

    /**
     * Reads any Components annotations.
     * @param componentsAnno
     */
    private Components readComponents(AnnotationValue componentsAnno) {
        if (componentsAnno == null) {
            return null;
        }
        LOG.debug("Processing an @Components annotation.");
        AnnotationInstance nested = componentsAnno.asNested();
        Components components = new ComponentsImpl();
        components.setCallbacks(readCallbacks(nested.value(ModelConstants.PROP_CALLBACKS)));
        components.setExamples(readExamples(nested.value(ModelConstants.PROP_EXAMPLES)));
        components.setHeaders(readHeaders(nested.value(ModelConstants.PROP_HEADERS)));
        components.setLinks(readLinks(nested.value(ModelConstants.PROP_LINKS)));
        components.setParameters(readParameters(nested.value(ModelConstants.PROP_PARAMETERS)));
        components.setRequestBodies(readRequestBodies(nested.value(ModelConstants.PROP_REQUEST_BODIES)));
        components.setResponses(readResponses(nested.value(ModelConstants.PROP_RESPONSES)));
        components.setSchemas(readSchemas(nested.value(ModelConstants.PROP_SCHEMAS)));
        components.setSecuritySchemes(readSecuritySchemes(nested.value(ModelConstants.PROP_SECURITY_SCHEMES)));
        return components;
    }

    /**
     * Reads a map of Callback annotations.
     * @param value
     */
    private Map<String, Callback> readCallbacks(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Callback annotations.");
        Map<String, Callback> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readCallback(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Callback annotation into a model.
     * @param annotation
     */
    private Callback readCallback(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Callback annotation.");
        Callback callback = new CallbackImpl();
        callback.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        String expression = AnnotationUtil.stringValue(annotation, PROP_CALLBACK_URL_EXPRESSION);
        callback.put(expression, readCallbackOperations(annotation.value(PROP_OPERATIONS)));
        return callback;
    }

    /**
     * Reads the CallbackOperation annotations as a PathItem.  The annotation value
     * in this case is an array of CallbackOperation annotations.
     * @param value
     */
    private PathItem readCallbackOperations(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing an array of @CallbackOperation annotations.");
        AnnotationInstance[] nestedArray = value.asNestedArray();
        PathItem pathItem = new PathItemImpl();
        for (AnnotationInstance operationAnno : nestedArray) {
            String method = AnnotationUtil.stringValue(operationAnno, PROP_METHOD);
            Operation operation = readCallbackOperation(operationAnno);
            if (method == null) {
                continue;
            }
            try {
                PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(pathItem, method.toUpperCase());
                Method mutator = PropertyUtils.getWriteMethod(descriptor);
                mutator.invoke(pathItem, operation);
            } catch (Exception e) {
                LOG.error("Error reading a CallbackOperation annotation.", e);
            }
        }
        return pathItem;
    }

    /**
     * Reads a single CallbackOperation annotation.
     * @param operationAnno
     * @return
     */
    private Operation readCallbackOperation(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @CallbackOperation annotation.");
        Operation operation = new OperationImpl();
        operation.setSummary(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_SUMMARY));
        operation.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        operation.setExternalDocs(readExternalDocs(annotation.value(ModelConstants.PROP_EXTERNAL_DOCS)));
        operation.setParameters(readOperationParameters(annotation.value(ModelConstants.PROP_PARAMETERS)));
        operation.setRequestBody(readRequestBody(annotation.value(ModelConstants.PROP_REQUEST_BODY)));
        operation.setResponses(readOperationResponses(annotation.value(ModelConstants.PROP_RESPONSES)));
        operation.setSecurity(readSecurity(annotation.value(ModelConstants.PROP_SECURITY)));
        operation.setExtensions(readExtensions(annotation.value(PROP_EXTENSIONS)));
        return operation;
    }

    /**
     * Reads an array of Parameter annotations into a list.
     * @param value
     */
    private List<Parameter> readOperationParameters(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of @Parameter annotations.");
        List<Parameter> parameters = new ArrayList<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            parameters.add(readParameter(nested));
        }
        return parameters;
    }

    /**
     * Reads an array of APIResponse annotations into an {@link APIResponses} model.
     * @param value
     */
    private APIResponses readOperationResponses(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of @APIResponse annotations into an APIResponses model.");
        APIResponses responses = new APIResponsesImpl();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                responses.put(name, readResponse(nested));
            }
        }
        return responses;
    }

    /**
     * Reads a map of Example annotations.
     * @param value
     */
    private Map<String, Example> readExamples(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @ExampleObject annotations.");
        Map<String, Example> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readExample(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Example annotation into a model.
     * @param annotation
     */
    private Example readExample(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @ExampleObject annotation.");
        Example example = new ExampleImpl();
        example.setSummary(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_SUMMARY));
        example.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        example.setValue(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_VALUE));
        example.setExternalValue(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_EXTERNAL_VALUE));
        example.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        return example;
    }

    /**
     * Reads a map of Header annotations.
     * @param value
     */
    private Map<String, Header> readHeaders(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Header annotations.");
        Map<String, Header> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readHeader(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Header annotation into a model.
     * @param annotation
     */
    private Header readHeader(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Header annotation.");
        Header header = new HeaderImpl();
        header.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        header.setSchema(readSchema(annotation.value(ModelConstants.PROP_SCHEMA)));
        header.setRequired(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_REQUIRED));
        header.setDeprecated(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_DEPRECATED));
        header.setAllowEmptyValue(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_ALLOW_EMPTY_VALUE));
        header.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        return header;
    }

    /**
     * Reads a map of Link annotations.
     * @param value
     */
    private Map<String, Link> readLinks(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Link annotations.");
        Map<String, Link> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readLink(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Link annotation into a model.
     * @param annotation
     */
    private Link readLink(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Link annotation.");
        Link link = new LinkImpl();
        link.setOperationRef(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_OPERATION_REF));
        link.setOperationId(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_OPERATION_ID));
        link.setParameters(readLinkParameters(annotation.value(ModelConstants.PROP_PARAMETERS)));
        link.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        link.setRequestBody(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_REQUEST_BODY));
        link.setServer(readServer(annotation.value(ModelConstants.PROP_SERVER)));
        link.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        return link;
    }

    /**
     * Reads an array of LinkParameter annotations into a map.
     * @param value
     */
    private Map<String, Object> readLinkParameters(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        AnnotationInstance[] nestedArray = value.asNestedArray();
        Map<String, Object> linkParams = new LinkedHashMap<>();
        for (AnnotationInstance annotation : nestedArray) {
            String name = AnnotationUtil.stringValue(annotation, ModelConstants.PROP_NAME);
            if (name != null) {
                String expression = AnnotationUtil.stringValue(annotation, PROP_EXPRESSION);
                linkParams.put(name, expression);
            }
        }
        return linkParams;
    }

    /**
     * Reads a map of Parameter annotations.
     * @param value
     */
    private Map<String, Parameter> readParameters(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Parameter annotations.");
        Map<String, Parameter> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                Parameter parameter = readParameter(nested);
                if (parameter != null) {
                    map.put(name, parameter);
                }
            }
        }
        return map;
    }

    /**
     * Reads a Parameter annotation into a model.
     * @param annotation
     */
    private Parameter readParameter(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Link annotation.");

        // Params can be hidden. Skip if that's the case.
        Boolean isHidden = AnnotationUtil.booleanValue(annotation, PROP_HIDDEN);
        if (isHidden != null && isHidden == Boolean.TRUE) {
            return null;
        }

        Parameter parameter = new ParameterImpl();
        parameter.setIn(AnnotationUtil.enumValue(annotation, ModelConstants.PROP_IN, org.eclipse.microprofile.openapi.models.parameters.Parameter.In.class));
        parameter.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        parameter.setRequired(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_REQUIRED));
        parameter.setDeprecated(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_DEPRECATED));
        parameter.setAllowEmptyValue(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(AnnotationUtil.enumValue(annotation, ModelConstants.PROP_STYLE, org.eclipse.microprofile.openapi.models.parameters.Parameter.Style.class));
        parameter.setExplode(readExplode(AnnotationUtil.enumValue(annotation, ModelConstants.PROP_EXPLODE, org.eclipse.microprofile.openapi.annotations.enums.Explode.class)));
        parameter.setAllowReserved(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_ALLOW_RESERVED));
        parameter.setSchema(readSchema(annotation.value(ModelConstants.PROP_SCHEMA)));
        parameter.setContent(readContent(annotation.value(ModelConstants.PROP_SERVER)));
        parameter.setExamples(readExamples(annotation.value(ModelConstants.PROP_EXAMPLES)));
        parameter.setExample(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_EXAMPLE));
        parameter.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        return parameter;
    }

    /**
     * Converts from an Explode enum to a true/false/null.
     * @param enumValue
     */
    private Boolean readExplode(Explode enumValue) {
        if (enumValue == Explode.TRUE) {
            return Boolean.TRUE;
        }
        if (enumValue == Explode.FALSE) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * Reads a single Content annotation into a model.  The value in this case is an array of
     * Content annotations.
     * @param value
     */
    private Content readContent(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a single @Content annotation.");
        Content content = new ContentImpl();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, PROP_MEDIA_TYPE);
            if (name != null) {
                MediaType mediaType = readMediaType(nested);
                content.addMediaType(name, mediaType);
            }
        }
        return content;
    }

    /**
     * Reads a single Content annotation into a {@link MediaType} model.
     * @param nested
     */
    private MediaType readMediaType(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Content annotation as a MediaType.");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setExamples(readExamples(annotation.value(ModelConstants.PROP_SERVER)));
        mediaType.setSchema(readSchema(annotation.value(ModelConstants.PROP_SCHEMA)));
        mediaType.setEncoding(readEncodings(annotation.value(ModelConstants.PROP_ENCODING)));
        return mediaType;
    }

    /**
     * Reads an array of Encoding annotations as a Map.
     * @param value
     */
    private Map<String, Encoding> readEncodings(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Encoding annotations.");
        Map<String, Encoding> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String name = AnnotationUtil.stringValue(annotation, ModelConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readEncoding(annotation));
            }
        }
        return map;
    }

    /**
     * Reads a single Encoding annotation into a model.
     * @param annotation
     */
    private Encoding readEncoding(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Encoding annotation.");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_CONTENT_TYPE));
        encoding.setStyle(AnnotationUtil.enumValue(annotation, ModelConstants.PROP_STYLE, org.eclipse.microprofile.openapi.models.media.Encoding.Style.class));
        encoding.setExplode(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_EXPLODE));
        encoding.setAllowReserved(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_ALLOW_RESERVED));
        encoding.setHeaders(readHeaders(annotation.value(ModelConstants.PROP_HEADERS)));
        return encoding;
    }

    /**
     * Reads a map of RequestBody annotations.
     * @param value
     */
    private Map<String, RequestBody> readRequestBodies(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @RequestBody annotations.");
        Map<String, RequestBody> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readRequestBody(nested));
            }
        }
        return map;
    }

    /**
     * Reads a RequestBody annotation into a model.
     * @param value
     */
    private RequestBody readRequestBody(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readRequestBody(value.asNested());
    }

    /**
     * Reads a RequestBody annotation into a model.
     * @param annotation
     */
    private RequestBody readRequestBody(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @RequestBody annotation.");
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        requestBody.setContent(readContent(annotation.value(ModelConstants.PROP_CONTENT)));
        requestBody.setRequired(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_REQUIRED));
        requestBody.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        return requestBody;
    }

    /**
     * Reads a map of APIResponse annotations.
     * @param value
     */
    private Map<String, APIResponse> readResponses(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @APIResponse annotations.");
        Map<String, APIResponse> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readResponse(nested));
            }
        }
        return map;
    }

    /**
     * Reads a APIResponse annotation into a model.
     * @param annotation
     */
    private APIResponse readResponse(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Response annotation.");
        APIResponse response = new APIResponseImpl();
        response.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        response.setHeaders(readHeaders(annotation.value(ModelConstants.PROP_HEADERS)));
        response.setLinks(readLinks(annotation.value(ModelConstants.PROP_LINKS)));
        response.setContent(readContent(annotation.value(ModelConstants.PROP_CONTENT)));
        response.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        return response;
    }

    /**
     * Reads a map of Schema annotations.
     * @param value
     */
    private Map<String, Schema> readSchemas(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Schema annotations.");
        Map<String, Schema> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readSchema(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Schema annotation into a model.
     * @param annotation
     */
    private Schema readSchema(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readSchema(value.asNested());
    }

    /**
     * Reads a Schema annotation into a model.
     * @param annotation
     */
    @SuppressWarnings("unchecked")
    private Schema readSchema(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Schema annotation.");

        // Schemas can be hidden. Skip if that's the case.
        Boolean isHidden = AnnotationUtil.booleanValue(annotation, PROP_HIDDEN);
        if (isHidden != null && isHidden == Boolean.TRUE) {
            return null;
        }

        Schema schema = new SchemaImpl();

        // TODO handle the case where the an implementation class is provided - that is introspected *first* and then augmented with the other info
        // TODO handle the case where an impl class is provided and the annotation "type" property is "array"

        //schema.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));  IMPLEMENTATION
        schema.setNot(readClassSchema(annotation.value(ModelConstants.PROP_NOT)));
        schema.setOneOf(readClassSchemas(annotation.value(ModelConstants.PROP_ONE_OF)));
        schema.setAnyOf(readClassSchemas(annotation.value(ModelConstants.PROP_ANY_OF)));
        schema.setAllOf(readClassSchemas(annotation.value(ModelConstants.PROP_ALL_OF)));
        schema.setTitle(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_TITLE));
        schema.setMultipleOf(AnnotationUtil.bigDecimalValue(annotation, ModelConstants.PROP_MULTIPLE_OF));
        schema.setMaximum(AnnotationUtil.bigDecimalValue(annotation, ModelConstants.PROP_MAXIMUM));
        schema.setExclusiveMaximum(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_EXCLUSIVE_MAXIMUM));
        schema.setMinimum(AnnotationUtil.bigDecimalValue(annotation, ModelConstants.PROP_MINIMUM));
        schema.setExclusiveMinimum(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_EXCLUSIVE_MINIMUM));
        schema.setMaxLength(AnnotationUtil.intValue(annotation, ModelConstants.PROP_MAX_LENGTH));
        schema.setMinLength(AnnotationUtil.intValue(annotation, ModelConstants.PROP_MIN_LENGTH));
        schema.setPattern(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_PATTERN));
        schema.setMaxProperties(AnnotationUtil.intValue(annotation, ModelConstants.PROP_MAX_PROPERTIES));
        schema.setMinProperties(AnnotationUtil.intValue(annotation, ModelConstants.PROP_MIN_PROPERTIES));
        schema.setRequired(AnnotationUtil.stringListValue(annotation, PROP_REQUIRED_PROPERTIES));
        schema.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        schema.setFormat(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_FORMAT));
        schema.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        schema.setNullable(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_NULLABLE));
        schema.setReadOnly(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_READ_ONLY));
        schema.setWriteOnly(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_WRITE_ONLY));
        schema.setExample(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_EXAMPLE));
        schema.setExternalDocs(readExternalDocs(annotation.value(ModelConstants.PROP_EXTERNAL_DOCS)));
        schema.setDeprecated(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_DEPRECATED));
        schema.setType(AnnotationUtil.enumValue(annotation, ModelConstants.PROP_TYPE, org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.class));
        schema.setEnumeration((List<Object>) (Object) AnnotationUtil.stringListValue(annotation, ModelConstants.PROP_ENUM));
        schema.setDefaultValue(AnnotationUtil.stringValue(annotation, PROP_DEFAULT_VALUE));
        schema.setDiscriminator(readDiscriminatorMappings(annotation.value(PROP_DISCRIMINATOR_MAPPING)));
        schema.setMaxItems(AnnotationUtil.intValue(annotation, ModelConstants.PROP_MAX_ITEMS));
        schema.setMinItems(AnnotationUtil.intValue(annotation, ModelConstants.PROP_MIN_ITEMS));
        schema.setUniqueItems(AnnotationUtil.booleanValue(annotation, ModelConstants.PROP_UNIQUE_ITEMS));
        return schema;
    }

    /**
     * Reads an array of Class annotations to produce a list of {@link Schema} models.
     * @param value
     */
    private List<Schema> readClassSchemas(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of schema Class annotations.");
        Type[] classArray = value.asClassArray();
        List<Schema> schemas = new ArrayList<>(classArray.length);
        for (Type type : classArray) {
            ClassType ctype = (ClassType) type;
            Schema schema = introspectClassToSchema(ctype);
            schemas.add(schema);
        }
        return schemas;
    }

    /**
     * Introspect into the given Class to generate a Schema model.
     * @param value
     */
    private Schema readClassSchema(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        ClassType ctype = (ClassType) value.asClass();
        Schema schema = introspectClassToSchema(ctype);
        return schema;
    }

    /**
     * Introspects the given class type to generate a Schema model.
     * @param ctype
     */
    private Schema introspectClassToSchema(ClassType ctype) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Reads an array of DiscriminatorMapping annotations into a {@link Discriminator} model.
     * @param value
     */
    private Discriminator readDiscriminatorMappings(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of @DiscriminatorMapping annotations.");
        Discriminator discriminator = new DiscriminatorImpl();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            // TODO iterate the discriminator mappings and do something sensible with them! :(
        }
        return discriminator;
    }

    /**
     * Reads a map of SecurityScheme annotations.
     * @param value
     */
    private Map<String, SecurityScheme> readSecuritySchemes(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @SecurityScheme annotations.");
        Map<String, SecurityScheme> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, PROP_SECURITY_SCHEME_NAME);
            if (name != null) {
                map.put(name, readSecurityScheme(nested));
            }
        }
        return map;
    }

    /**
     * Reads a APIResponse annotation into a model.
     * @param annotation
     */
    private SecurityScheme readSecurityScheme(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @SecurityScheme annotation.");
        SecurityScheme securityScheme = new SecuritySchemeImpl();
        securityScheme.setType(AnnotationUtil.enumValue(annotation, ModelConstants.PROP_TYPE, org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type.class));
        securityScheme.setDescription(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_DESCRIPTION));
        securityScheme.setIn(AnnotationUtil.enumValue(annotation, ModelConstants.PROP_IN, org.eclipse.microprofile.openapi.models.security.SecurityScheme.In.class));
        securityScheme.setScheme(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_SCHEME));
        securityScheme.setBearerFormat(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_BEARER_FORMAT));
        securityScheme.setFlows(readOAuthFlows(annotation.value(ModelConstants.PROP_FLOWS)));
        securityScheme.setOpenIdConnectUrl(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_OPEN_ID_CONNECT_URL));
        securityScheme.setRef(AnnotationUtil.stringValue(annotation, PROP_REF));
        return securityScheme;
    }

    /**
     * Reads an OAuthFlows annotation into a model.
     * @param value
     */
    private OAuthFlows readOAuthFlows(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a single @OAuthFlows annotation.");
        AnnotationInstance annotation = value.asNested();
        OAuthFlows flows = new OAuthFlowsImpl();
        flows.setImplicit(readOAuthFlow(annotation.value(ModelConstants.PROP_IMPLICIT)));
        flows.setPassword(readOAuthFlow(annotation.value(ModelConstants.PROP_PASSWORD)));
        flows.setClientCredentials(readOAuthFlow(annotation.value(ModelConstants.PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(readOAuthFlow(annotation.value(ModelConstants.PROP_AUTHORIZATION_CODE)));
        return flows;
    }

    /**
     * Reads a single OAuthFlow annotation into a model.
     * @param value
     */
    private OAuthFlow readOAuthFlow(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a single @OAuthFlow annotation.");
        AnnotationInstance annotation = value.asNested();
        OAuthFlow flow = new OAuthFlowImpl();
        flow.setAuthorizationUrl(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_TOKEN_URL));
        flow.setRefreshUrl(AnnotationUtil.stringValue(annotation, ModelConstants.PROP_REFRESH_URL));
        flow.setScopes(readOAuthScopes(annotation.value(ModelConstants.PROP_SCOPES)));
        return flow;
    }

    /**
     * Reads an array of OAuthScope annotations into a Scopes model.
     * @param value
     */
    private Scopes readOAuthScopes(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of @OAuthScope annotations.");
        AnnotationInstance[] nestedArray = value.asNestedArray();
        Scopes scopes = new ScopesImpl();
        for (AnnotationInstance nested : nestedArray) {
            String name = AnnotationUtil.stringValue(nested, ModelConstants.PROP_NAME);
            if (name != null) {
                String description = AnnotationUtil.stringValue(nested, ModelConstants.PROP_DESCRIPTION);
                scopes.addScope(name, description);
            }
        }
        return scopes;
    }

    /**
     * Reads an array of Extension annotations.  The AnnotationValue in this case is
     * an array of Extension annotations.  These must be read and converted into a Map.
     * @param value
     */
    private Map<String, Object> readExtensions(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> extensions = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String extName = AnnotationUtil.stringValue(annotation, ModelConstants.PROP_NAME);
            String extValue = AnnotationUtil.stringValue(annotation, ModelConstants.PROP_VALUE);
            extensions.put(extName, extValue);
        }
        return extensions;
    }

}
