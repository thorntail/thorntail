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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.microprofile.openapi.io.ModelConstants;
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
     * Reads a {@link OpenAPIDefinition} annotation.
     * @param openApi
     * @param definitionAnno
     */
    protected void processDefinition(OpenAPIImpl openApi, AnnotationInstance definitionAnno) {
        LOG.debug("Processing an @OpenAPIDefinition annotation.");
        openApi.setInfo(readInfo(definitionAnno.value("info")));
        openApi.setTags(readTags(definitionAnno.value("tags")));
        openApi.setServers(readServers(definitionAnno.value("servers")));
        openApi.setSecurity(readSecurity(definitionAnno.value("security")));
        openApi.setExternalDocs(readExternalDocs(definitionAnno.value("externalDocs")));
        openApi.setComponents(readComponents(definitionAnno.value("components")));
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
            variables.addServerVariable(name, readServerVariable(serverVariableAnno));
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
     * Reads any {@link SecurityRequirement} annotations.  The annotation value is an array of
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
            List<String> scopes = AnnotationUtil.stringListValue(requirementAnno, ModelConstants.PROP_SCOPES);
            SecurityRequirement requirement = new SecurityRequirementImpl();
            if (scopes == null) {
                requirement.addScheme(name);
            } else {
                requirement.addScheme(name, scopes);
            }
            requirements.add(requirement);
        }
        return requirements;
    }

    /**
     * Reads an {@link ExternalDocumentation} annotation.
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
     * Reads any {@link Components} annotations.
     * @param componentsAnno
     */
    private Components readComponents(AnnotationValue componentsAnno) {
        if (componentsAnno == null) {
            return null;
        }
        LOG.debug("Processing an @Components annotation.");
        return null;
    }

}
