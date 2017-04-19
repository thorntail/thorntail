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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.javaee7.ListenerType;
import org.jboss.shrinkwrap.descriptor.api.javaee7.ParamValueType;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon31.LoginConfigType;
import org.jboss.shrinkwrap.descriptor.api.webcommon31.ServletType;

import static org.wildfly.swarm.spi.api.ClassLoading.withTCCL;

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

    public Servlet addServlet(String servletName, String servletClass) {
        Servlet servlet = new Servlet(servletName, servletClass);
        this.servlets.add(servlet);
        return servlet;
    }

    public Servlet getServlet(String servletClass) {
        ServletType<WebAppDescriptor> descriptorServlet = this.descriptor.getAllServlet()
                .stream()
                .filter(s -> s.getServletClass().equals(servletClass))
                .findFirst().get();

        return convert(descriptorServlet);
    }

    public void setLoginConfig(String authMethod, String realmName) {
        this.descriptor.getOrCreateLoginConfig()
                .authMethod(authMethod)
                .realmName(realmName);
    }

    public void setFormLoginConfig(String realmName, String loginPage, String errorPage) {
        this.descriptor.getOrCreateLoginConfig()
                .authMethod("FORM")
                .realmName(realmName)
                .getOrCreateFormLoginConfig()
                .formLoginPage(loginPage)
                .formErrorPage(errorPage);
    }

    public String getLoginRealm(String authMethod) {
        if (authMethod == null || authMethod.length() == 0) {
            return null;
        }

        Optional<LoginConfigType<WebAppDescriptor>> loginConfig = this.descriptor.getAllLoginConfig().stream().filter(l -> l.getAuthMethod().equals(authMethod)).findFirst();

        return loginConfig.isPresent() ? loginConfig.get().getRealmName() : null;
    }

    public void addListener(String listener) {
        this.descriptor.createListener()
                .listenerClass(listener);
    }

    public List<String> allListenersClasses() {
        return this.descriptor.getAllListener().stream().map(ListenerType::getListenerClass).collect(Collectors.toList());
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

    /**
     *
     * @param servletName
     * @return the list of <code>url-pattern</code> elements or an empty list if no mapping is present
     */
    public List<String> getServletMapping(String servletName) {
        return this.descriptor.getAllServletMapping().stream().filter((mapping) -> mapping.getServletName().equals(servletName)).findFirst()
                .map(m -> m.getAllUrlPattern()).orElse(Collections.emptyList());
    }

    @Override
    public InputStream openStream() {
        // Add Security Constraints
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

        // Add Servlets
        for (Servlet each : this.servlets) {
            ServletType<WebAppDescriptor> servlet =
                    this.descriptor.createServlet()
                            .servletName(each.servletName())
                            .servletClass(each.servletClass())
                            .displayName(each.displayName())
                            .enabled(each.enabled());

            if (each.asyncSupported() != null) {
                servlet.asyncSupported(each.asyncSupported());
            }

            if (each.loadOnStartup() != null) {
                servlet.loadOnStartup(each.loadOnStartup());
            }

            for (Map.Entry<String, String> init : each.initParams().entrySet()) {
                servlet.createInitParam()
                        .paramName(init.getKey())
                        .paramValue(init.getValue());
            }

            if (each.urlPatterns().size() > 0) {
                this.descriptor.createServletMapping()
                        .servletName(each.servletName())
                        .urlPattern(each.urlPatterns().stream().toArray(String[]::new));
            }
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

    private Servlet convert(ServletType<WebAppDescriptor> descriptorServlet) {
        Servlet servlet = new Servlet(descriptorServlet.getServletName(), descriptorServlet.getServletClass());

        List<String> dispNames = descriptorServlet.getAllDisplayName();
        if (dispNames.size() > 0) {
            servlet.withDisplayName(dispNames.get(0));
        }
        List<String> descriptions = descriptorServlet.getAllDescription();
        if (descriptions.size() > 0) {
            servlet.withDescription(descriptions.get(0));
        }
        servlet.withEnabled(descriptorServlet.isEnabled());
        servlet.withAsyncSupported(descriptorServlet.isAsyncSupported());
        servlet.withLoadOnStartup(descriptorServlet.getLoadOnStartup());
        servlet.withInitParams(
                descriptorServlet.getAllInitParam()
                        .stream()
                        .collect(Collectors.toMap(
                                ParamValueType::getParamName,
                                ParamValueType::getParamValue)
                        )
        );
        servlet.withUrlPatterns(this.descriptor.getAllServletMapping()
                                        .stream()
                                        .filter(mapping -> mapping.getServletName().equals(servlet.servletName()))
                                        .findFirst().get().getAllUrlPattern());
        return servlet;
    }

    private final WebAppDescriptor descriptor;

    private List<SecurityConstraint> constraints = new ArrayList<>();

    private List<Servlet> servlets = new ArrayList<>();
}
