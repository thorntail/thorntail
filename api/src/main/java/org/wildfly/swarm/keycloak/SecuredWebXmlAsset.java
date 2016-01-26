/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.keycloak;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.wildfly.swarm.container.util.XmlWriter;

/**
 * @author Bob McWhirter
 */
public class SecuredWebXmlAsset implements NamedAsset {

    public static final String NAME = "WEB-INF/web.xml";

    private List<SecurityConstraint> constraints = new ArrayList<>();

    public SecuredWebXmlAsset() {

    }

    public SecurityConstraint protect() {
        SecurityConstraint constraint = new SecurityConstraint();
        this.constraints.add(constraint);
        return constraint;
    }

    public SecurityConstraint protect(String urlPattern) {
        SecurityConstraint constraint = new SecurityConstraint(urlPattern);
        this.constraints.add(constraint);
        return constraint;
    }

    @Override
    public InputStream openStream() {
        StringWriter out = new StringWriter();
        XmlWriter writer = new XmlWriter(out);
        try {
            XmlWriter.Element webApp = writer.element("web-app");
            webApp.attr("xmlns", "http://java.sun.com/xml/ns/javaee");
            webApp.attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            webApp.attr("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd");
            webApp.attr("version", "3.0");

            XmlWriter.Element param = webApp.element("context-param");
            param.element("param-name").content("resteasy.scan").end();
            param.element("param-value").content("true").end();
            param.end();

            Set<String> allRoles = new HashSet<>();

            for (SecurityConstraint each : this.constraints) {
                XmlWriter.Element securityConstraint = webApp.element("security-constraint");

                XmlWriter.Element webResourceCollection = securityConstraint.element("web-resource-collection");
                webResourceCollection.element("url-pattern").content(each.urlPattern()).end();
                for (String eachMethod : each.methods()) {
                    webResourceCollection.element("http-method").content(eachMethod).end();
                }
                webResourceCollection.end();

                XmlWriter.Element authConstraint = securityConstraint.element("auth-constraint");
                for (String eachRole : each.roles()) {
                    authConstraint.element("role-name").content(eachRole).end();
                    allRoles.add(eachRole);
                }
                authConstraint.end();

                securityConstraint.end();
            }

            XmlWriter.Element loginConfig = webApp.element("login-config");
            loginConfig.element("auth-method").content("KEYCLOAK").end();
            loginConfig.element("realm-name").content("ignored").end();
            loginConfig.end();

            for (String eachRole : allRoles) {
                XmlWriter.Element securityRole = webApp.element("security-role");
                securityRole.element("role-name").content(eachRole).end();
                securityRole.end();
            }

            webApp.end();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(out.toString().getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
