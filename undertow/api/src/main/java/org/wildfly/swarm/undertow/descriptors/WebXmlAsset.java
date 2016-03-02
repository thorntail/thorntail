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
package org.wildfly.swarm.undertow.descriptors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.javaee7.ParamValueType;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon31.LoginConfigType;

import static org.wildfly.swarm.container.util.ClassLoading.withTCCL;

/**
 * @author Ken Finnigan
 */
public class WebXmlAsset implements NamedAsset {

    public static final String NAME = "WEB-INF/web.xml";

    public WebXmlAsset() {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                         () -> Descriptors.create(WebAppDescriptor.class));
    }

    public WebXmlAsset(InputStream fromStream) {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                         () -> Descriptors.importAs(WebAppDescriptor.class)
                                 .fromStream(fromStream));
    }

    public void setContextParam(String name, String... values) {
        this.descriptor.createContextParam()
                .paramName(name)
                .paramValue(convert(values));
    }

    public String getContextParam(String name) {
        if (name == null || name.length() == 0) {
            return null;
        }

        Optional<ParamValueType<WebAppDescriptor>> paramValue = this.descriptor.getAllContextParam().stream().filter(p -> p.getParamName().equals(name)).findFirst();

        return paramValue.isPresent() ? paramValue.get().getParamValue() : null;
    }

    public void setLoginConfig(String authMethod, String realmName) {
        this.descriptor.createLoginConfig()
                .authMethod(authMethod)
                .realmName(realmName);
    }

    public String getLoginRealm(String authMethod) {
        if (authMethod == null || authMethod.length() == 0) {
            return null;
        }

        Optional<LoginConfigType<WebAppDescriptor>> loginConfig = this.descriptor.getAllLoginConfig().stream().filter(l -> l.getAuthMethod().equals(authMethod)).findFirst();

        return loginConfig.isPresent() ? loginConfig.get().getRealmName() : null;
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
        Set<String> allRoles = new HashSet<>();

        for (SecurityConstraint each : this.constraints) {
            this.descriptor.createSecurityConstraint()
                    .createWebResourceCollection()
                    .urlPattern(each.urlPattern())
                    .httpMethod(each.methods().toArray(new String[each.methods().size()]))
                    .up()
                    .getOrCreateAuthConstraint()
                    .roleName(each.roles().toArray(new String[each.roles().size()]))
                    .up();

            allRoles.addAll(each.roles());
        }

        for (String eachRole : allRoles) {
            this.descriptor.getOrCreateSecurityRole()
                    .roleName(eachRole);
        }

        return new ByteArrayInputStream(this.descriptor.exportAsString().getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }

    private String convert(String... values) {
        StringBuilder builder = new StringBuilder();
        boolean addedFirst = false;

        for (String value : values) {
            if (addedFirst) {
                builder.append(',');
            }
            builder.append(value);
            addedFirst = true;
        }

        return builder.toString();
    }

    private final WebAppDescriptor descriptor;

    private List<SecurityConstraint> constraints = new ArrayList<>();
}
