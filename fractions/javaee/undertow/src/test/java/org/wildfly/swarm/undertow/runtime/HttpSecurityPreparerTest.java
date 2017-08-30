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
package org.wildfly.swarm.undertow.runtime;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;
import org.yaml.snakeyaml.Yaml;

import static org.fest.assertions.Assertions.assertThat;

public class HttpSecurityPreparerTest {

    private HttpSecurityPreparer preparer;

    private WARArchive archive;

    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @Before
    public void setUp() {
        archive = ShrinkWrap.create(WARArchive.class, "app.war");
        preparer = new HttpSecurityPreparer(archive);
    }

    @Test
    public void do_nothing_if_not_specified_security_constraints() throws Exception {
        preparer.process();
        assertThat(archive.get(WebXmlAsset.NAME)).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void yaml_parsing() throws Exception {

        InputStream in = getClass().getClassLoader().getResourceAsStream("security.yml");
        assertThat(in).isNotNull().as("security.yml is null");
        Yaml yaml = new Yaml();
        Map<String, Object> httpConfig = (Map<String, Object>) yaml.load(in);

        preparer.deploymentConfigs = (Map) ((Map) httpConfig.get("swarm")).get("deployment");
        preparer.process();

        WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(archive.get(WebXmlAsset.NAME).getAsset().openStream());

        assertThat(webXml.getAllSecurityConstraint().size()).isEqualTo(1);
        assertThat(webXml.getAllSecurityConstraint().get(0).getAllWebResourceCollection().get(0).getAllUrlPattern().get(0)).isEqualTo("/protected");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void yaml_parsing_again() throws Exception {

        InputStream in = getClass().getClassLoader().getResourceAsStream("security2.yml");
        assertThat(in).isNotNull().as("security2.yml is null");
        Yaml yaml = new Yaml();
        Map<String, Object> httpConfig = (Map<String, Object>) yaml.load(in);

        preparer.deploymentConfigs = (Map) ((Map) httpConfig.get("swarm")).get("deployment");
        preparer.process();

        WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(archive.get(WebXmlAsset.NAME).getAsset().openStream());

        assertThat(webXml.getAllSecurityConstraint().size()).isEqualTo(1);
        assertThat(webXml.getAllSecurityConstraint().get(0).getAllWebResourceCollection().get(0).getAllUrlPattern().get(0)).isEqualTo("/protected");
    }

    @Test
    public void unsupported_auth_method() throws Exception {
        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> loginConfig = new HashMap<>();
        loginConfig.put("auth-method", "foobar");
        webConfig.put("login-config", loginConfig);

        preparer.deploymentConfigs = deploymentConfig;
        preparer.process();

        WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(archive.get(WebXmlAsset.NAME).getAsset().openStream());
        assertThat(webXml.getOrCreateLoginConfig().getAuthMethod()).isEqualTo("foobar");
    }

    @Test
    public void set_1_security_constraint() throws Exception {
        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> securityConstraint = new HashMap<>();
        securityConstraint.put("url-pattern", "/aaa");

        webConfig.put("security-constraints", Collections.singletonList(securityConstraint));

        preparer.deploymentConfigs = deploymentConfig;
        preparer.process();

        WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(archive.get(WebXmlAsset.NAME).getAsset().openStream());

        assertThat(webXml.getAllSecurityConstraint().size()).isEqualTo(1);
        assertThat(webXml.getAllSecurityConstraint().get(0).getAllWebResourceCollection().get(0).getAllUrlPattern().get(0)).isEqualTo("/aaa");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findWebConfig(Map<String, Object> deploymentConfig) {
        String[] path = new String[]{archive.getName(), "web"};
        Map<String, Object> curr = deploymentConfig;

        for (int i = 0; i < path.length; i++) {
            curr = (Map<String, Object>) curr.get(path[i]);
        }

        return curr;
    }

    private Map<String, Object> createConfigStub() {
        HashMap<String, Object> deploymentConfig = new HashMap<>();
        HashMap<String, Object> archiveConfig = new HashMap<>();
        HashMap<String, Object> webConfig = new HashMap<>();

        Map<String, Object> loginConfig = new HashMap<>();
        loginConfig.put("auth-method", "BASIC");

        archiveConfig.put("web", webConfig);
        webConfig.put("login-config", loginConfig);
        deploymentConfig.put(archive.getName(), archiveConfig);

        return deploymentConfig;
    }

    @Test
    public void set_2_security_constraints() throws Exception {

        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> securityConstraint1 = new HashMap<>();
        securityConstraint1.put("url-pattern", "/aaa");
        Map<String, Object> securityConstraint2 = new HashMap<>();
        securityConstraint2.put("url-pattern", "/bbb");


        webConfig.put("security-constraints", Arrays.asList(securityConstraint1, securityConstraint2));
        preparer.deploymentConfigs = deploymentConfig;
        preparer.process();

        WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(archive.get(WebXmlAsset.NAME).getAsset().openStream());

        assertThat(webXml.getAllSecurityConstraint().size()).isEqualTo(2);
        assertThat(webXml.getAllSecurityConstraint().get(0).getAllWebResourceCollection().get(0).getAllUrlPattern().get(0)).isEqualTo("/aaa");
        assertThat(webXml.getAllSecurityConstraint().get(1).getAllWebResourceCollection().get(0).getAllUrlPattern().get(0)).isEqualTo("/bbb");
    }

    @Test
    public void set_1_method() throws Exception {
        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> securityConstraint = new HashMap<>();
        securityConstraint.put("methods", Arrays.asList("GET"));


        webConfig.put("security-constraints", Collections.singletonList(securityConstraint));
        preparer.deploymentConfigs = deploymentConfig;
        preparer.process();

        WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(archive.get(WebXmlAsset.NAME).getAsset().openStream());

        assertThat(webXml.getAllSecurityConstraint().size()).isEqualTo(1);
        assertThat(webXml.getAllSecurityConstraint().get(0).getAllWebResourceCollection().get(0).getAllHttpMethod().get(0)).isEqualTo("GET");
    }

    @Test
    public void set_2_methods() throws Exception {

        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> securityConstraint = new HashMap<>();
        securityConstraint.put("methods", Arrays.asList("GET", "POST"));


        webConfig.put("security-constraints", Collections.singletonList(securityConstraint));
        preparer.deploymentConfigs = deploymentConfig;
        preparer.process();

        WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(archive.get(WebXmlAsset.NAME).getAsset().openStream());

        assertThat(webXml.getAllSecurityConstraint().size()).isEqualTo(1);
        assertThat(webXml.getAllSecurityConstraint().get(0).getAllWebResourceCollection().get(0).getAllHttpMethod().get(0)).isEqualTo("GET");
        assertThat(webXml.getAllSecurityConstraint().get(0).getAllWebResourceCollection().get(0).getAllHttpMethod().get(1)).isEqualTo("POST");
    }

}
