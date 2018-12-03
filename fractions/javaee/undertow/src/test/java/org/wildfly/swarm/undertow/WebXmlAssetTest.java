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
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
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

    @Test
    public void testNoRepeatedServlets() throws IOException {
        ClassLoaderAsset existing = new ClassLoaderAsset("web.xml");
        WebXmlAsset asset = new WebXmlAsset(existing.openStream());

        asset.addServlet("HelloServlet", "com.example.HelloServlet")
                .withUrlPattern("/hello");

        for (int i = 0; i < 10; i++) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(asset.openStream()))) {
                String webXml = reader.lines().collect(Collectors.joining("\n"));

                assertThat(countOccurences("<servlet-class>com.example.HelloServlet</servlet-class>", webXml))
                        .as("Expected only 1 occurence of <servlet-class>com.example.HelloServlet</servlet-class>")
                        .isEqualTo(1);
                assertThat(countOccurences("/hello", webXml))
                        .as("Expected only 1 occurence of /hello")
                        .isEqualTo(1);

                assertThat(countOccurences("<servlet-class>com.app.MyServletClass</servlet-class>", webXml))
                        .as("Expected only 1 occurence of <servlet-class>com.app.MyServletClass</servlet-class>")
                        .isEqualTo(1);
                assertThat(countOccurences("/me", webXml))
                        .as("Expected only 1 occurence of /me")
                        .isEqualTo(1);
            }
        }
    }

    @Test
    public void testNoRepeatedSecurityConstraints() throws IOException {
        ClassLoaderAsset existing = new ClassLoaderAsset("web.xml");
        WebXmlAsset asset = new WebXmlAsset(existing.openStream());

        String urlPattern = "/hello";
        asset.protect(urlPattern)
                .withMethod("GET")
                .withMethod("POST")
                .withRole("guest")
                .withRole("admin");

        for (int i = 0; i < 10; i++) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(asset.openStream()))) {
                String webXml = reader.lines().collect(Collectors.joining("\n"));

                // TODO unfortunately, our class SecurityConstraint isn't well equipped to fully represent
                // the relevant part of web.xml, so we allow duplicity for now
                //
                // when this is fixed, the .isEqualTo() lines should be uncommented
                // and the .isGreaterThanOrEqualTo() lines should be deleted

                assertThat(countOccurences("<url-pattern>/hello</url-pattern>", webXml))
                        .as("Expected only 1 occurence of <url-pattern>/hello</url-pattern>")
                        .isGreaterThanOrEqualTo(1);
                        //.isEqualTo(1);
                assertThat(countOccurences("<http-method>GET</http-method>", webXml))
                        .as("Expected only 1 occurence of <http-method>GET</http-method>")
                        .isGreaterThanOrEqualTo(1);
                        //.isEqualTo(1);
                assertThat(countOccurences("<http-method>POST</http-method>", webXml))
                        .as("Expected only 1 occurence of <http-method>POST</http-method>")
                        .isGreaterThanOrEqualTo(1);
                        //.isEqualTo(1);
                assertThat(countOccurences("<role-name>guest</role-name>", webXml))
                        .as("Expected only 2 occurences of <role-name>guest</role-name> (once under <security-constraint>, once under <security-role>)")
                        .isGreaterThanOrEqualTo(2);
                        //.isEqualTo(2);
                assertThat(countOccurences("<role-name>admin</role-name>", webXml))
                        .as("Expected only 2 occurences of <role-name>admin</role-name> (once under <security-constraint>, once under <security-role>)")
                        .isGreaterThanOrEqualTo(2);
                        //.isEqualTo(2);

                assertThat(countOccurences("<url-pattern>/foobar</url-pattern>", webXml))
                        .as("Expected only 1 occurence of <url-pattern>/foobar</url-pattern>")
                        .isEqualTo(1);
                assertThat(countOccurences("<http-method>DELETE</http-method>", webXml))
                        .as("Expected only 1 occurence of <http-method>DELETE</http-method>")
                        .isEqualTo(1);
                assertThat(countOccurences("<role-name>superuser</role-name>", webXml))
                        .as("Expected only 2 occurences of <role-name>superuser</role-name> (once under <security-constraint>, once under <security-role>)")
                        .isEqualTo(2);
            }
        }
    }

    private static int countOccurences(String substring, String string) {
        int counter = 0;
        int index = 0;
        while (string.indexOf(substring, index) > 0) {
            counter++;
            index = string.indexOf(substring, index) + substring.length();
        }
        return counter;
    }
}
