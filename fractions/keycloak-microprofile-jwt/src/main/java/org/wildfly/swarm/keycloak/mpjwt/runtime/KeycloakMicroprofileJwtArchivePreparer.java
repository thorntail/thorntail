/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.keycloak.mpjwt.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

@DeploymentScoped
public class KeycloakMicroprofileJwtArchivePreparer implements DeploymentProcessor {

    private static final Logger log = Logger.getLogger(KeycloakMicroprofileJwtArchivePreparer.class);
    private final Archive<?> archive;

    @Inject
    public KeycloakMicroprofileJwtArchivePreparer(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws IOException {
        InputStream keycloakJsonStream = getKeycloakJsonFromClasspath("keycloak.json");
        if (keycloakJsonStream == null) {
            keycloakJsonStream = getKeycloakJsonFromSystemProperties();
        }
        if (keycloakJsonStream != null) {
            try {
                Module module = Module.getBootModuleLoader().loadModule("org.wildfly.swarm.keycloak.mpjwt:deployment");
                Class<?> use = module.getClassLoader()
                    .loadClass("org.wildfly.swarm.keycloak.mpjwt.deployment.KeycloakJWTCallerPrincipalFactory");
                Method m = use.getDeclaredMethod("createDeploymentFromStream", InputStream.class);
                m.invoke(null, keycloakJsonStream);
            } catch (Throwable ex) {
                log.warn("keycloak.json resource is not available", ex);
            }
        }
    }

    private InputStream getKeycloakJsonFromSystemProperties() {
        final String thorntailPrefix = "thorntail.keycloak.secure-deployments.[" + archive.getName() + "].";
        final String swarmPrefix = "swarm.keycloak.secure-deployments.[" + archive.getName() + "].";
        final String credentialsSecretProperty = "credentials.secret.value";

        Map<String, String> kcAdapterSystemProps = new LinkedHashMap<>();
        Properties systemProps = System.getProperties();
        for (Map.Entry<Object, Object> entry : systemProps.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith(thorntailPrefix)) {
                kcAdapterSystemProps.put(key.substring(thorntailPrefix.length()),
                                         entry.getValue().toString());
            } else if (key.startsWith(swarmPrefix)) {
                kcAdapterSystemProps.put(key.substring(swarmPrefix.length()),
                                         entry.getValue().toString());
            }
        }
        InputStream is = null;
        if (!kcAdapterSystemProps.isEmpty()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                JsonGenerator jg = new JsonFactory().createGenerator(out, JsonEncoding.UTF8);
                jg.writeStartObject();
                for (Map.Entry<String, String> entry : kcAdapterSystemProps.entrySet()) {
                    if (entry.getKey().equals(credentialsSecretProperty)) {
                        jg.writeFieldName("credentials");
                        jg.writeStartObject();
                        jg.writeFieldName("secret");
                        jg.writeString(entry.getValue());
                        jg.writeEndObject();
                    } else {
                        jg.writeFieldName(entry.getKey());
                        if (isBoolean(entry.getValue())) {
                            jg.writeBoolean(Boolean.valueOf(entry.getValue()));
                        } else if (isSimpleNumber(entry.getValue())) {
                            jg.writeNumber(Long.valueOf(entry.getValue()));
                        } else {
                            jg.writeString(entry.getValue());
                        }
                    }
                }
                jg.writeEndObject();
                jg.close();
                is = new ByteArrayInputStream(out.toByteArray());
            } catch (IOException ex) {
                log.warn("keycloak.json can not be auto-generated", ex);
            }
        }
        return is;
    }

    private static boolean isSimpleNumber(String value) {
        return value.matches("\\d+");
    }
    private static boolean isBoolean(String value) {
        return "true".equals(value) || "false".equals(value);
    }

    private InputStream getKeycloakJsonFromClasspath(String resourceName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream keycloakJson = cl.getResourceAsStream(resourceName);
        if (keycloakJson == null) {

            Node jsonNode = archive.get(resourceName);
            if (jsonNode == null) {
                jsonNode = getKeycloakJsonNodeFromWebInf(resourceName, true);
            }
            if (jsonNode == null) {
                jsonNode = getKeycloakJsonNodeFromWebInf(resourceName, false);
            }
            if (jsonNode != null && jsonNode.getAsset() != null) {
                keycloakJson = jsonNode.getAsset().openStream();
            }
        }
        return keycloakJson;
    }

    private Node getKeycloakJsonNodeFromWebInf(String resourceName,
            boolean useForwardSlash) {
        String webInfPath = useForwardSlash ? "/WEB-INF" : "WEB-INF";
        if (!resourceName.startsWith("/")) {
            resourceName = "/" + resourceName;
        }
        Node jsonNode = archive.get(webInfPath + resourceName);
        if (jsonNode == null) {
            jsonNode = archive.get(webInfPath + "/classes" + resourceName);
        }
        return jsonNode;
    }

}
