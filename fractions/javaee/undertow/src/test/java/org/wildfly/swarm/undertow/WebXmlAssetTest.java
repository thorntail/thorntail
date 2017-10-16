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
package org.wildfly.swarm.undertow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon31.SecurityConstraintType;
import org.junit.Test;
import org.wildfly.swarm.undertow.descriptors.SecurityConstraint;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class WebXmlAssetTest {

    @Test
    public void testInvalidContextParamName() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getContextParam(null)).isNull();
        assertThat(asset.getContextParam("")).isNull();
        assertThat(asset.getContextParam("   ")).isNull();
    }

    @Test
    public void testUnsetContextParamName() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getContextParam("my-param")).isNull();
    }

    @Test
    public void testSingleContextParamValue() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getContextParam("my-param")).isNull();

        asset.setContextParam("my-param", "myValue");
        String paramValue = asset.getContextParam("my-param");
        assertThat(paramValue).isNotNull();
        assertThat(paramValue).isNotEmpty();
        assertThat(paramValue).isEqualTo("myValue");
    }

    @Test
    public void testMultipleContextParamValue() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getContextParam("my-param")).isNull();

        asset.setContextParam("my-param", "myValue1", "myValue2", "myValue3");
        String paramValue = asset.getContextParam("my-param");
        assertThat(paramValue).isNotNull();
        assertThat(paramValue).isNotEmpty();
        assertThat(paramValue).isEqualTo("myValue1,myValue2,myValue3");
    }

    @Test
    public void testInvalidLoginAuthMethod() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getLoginRealm(null)).isNull();
        assertThat(asset.getLoginRealm("")).isNull();
        assertThat(asset.getLoginRealm("  ")).isNull();
    }

    @Test
    public void testUnsetLoginAuthMethod() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getLoginRealm("keycloak")).isNull();
    }

    @Test
    public void testLoginAuthMethod() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getLoginRealm("keycloak")).isNull();

        asset.setLoginConfig("keycloak", "myRealm");
        String realm = asset.getLoginRealm("keycloak");
        assertThat(realm).isNotNull();
        assertThat(realm).isNotEmpty();
        assertThat(realm).isEqualTo("myRealm");
    }

    @Test
    public void testFormLoginConfig() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        asset.setFormLoginConfig("myRealm", "/login", "/error");
        assertThat(asset.getLoginRealm("FORM")).isEqualTo("myRealm");
    }

    @Test
    public void testSecurityConstraintPermitAll() throws IOException {
        WebXmlAsset asset = new WebXmlAsset();

        asset.protect().permitAll();
        List<SecurityConstraint> constraints = asset.allConstraints();
        assertThat(constraints).hasSize(1);
        SecurityConstraint sc = constraints.get(0);
        assertThat(sc.isPermitAll()).isEqualTo(true);

        InputStreamReader reader = new InputStreamReader(asset.openStream());
        BufferedReader buffered = new BufferedReader(reader);
        StringBuilder tmp = new StringBuilder();
        String line = buffered.readLine();
        while (line != null) {
            tmp.append(line);
            line = buffered.readLine();
        }
        assertThat(tmp.toString()).contains("security-constraint");
        assertThat(tmp.toString()).doesNotContain("auth-constraint");
    }
}
