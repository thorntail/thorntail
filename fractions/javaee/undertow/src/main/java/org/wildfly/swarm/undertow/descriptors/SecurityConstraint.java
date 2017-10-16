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
package org.wildfly.swarm.undertow.descriptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class SecurityConstraint {

    public SecurityConstraint() {
        this("/*");
    }

    public SecurityConstraint(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String urlPattern() {
        return this.urlPattern;
    }

    public SecurityConstraint withMethod(String method) {
        this.methods.add(method);
        return this;
    }

    public SecurityConstraint withMethod(String... methods) {
        this.methods.addAll(Arrays.asList(methods));
        return this;
    }

    public List<String> methods() {
        return this.methods;
    }

    public SecurityConstraint withRole(String role) {
        this.roles.add(role);
        return this;
    }

    public SecurityConstraint withRole(String... roles) {
        this.roles.addAll(Arrays.asList(roles));
        return this;
    }

    public SecurityConstraint permitAll() {
        this.permitAll = true;
        return this;
    }
    public boolean isPermitAll() {
        return permitAll;
    }

    public List<String> roles() {
        return this.roles;
    }

    private final String urlPattern;

    private List<String> methods = new ArrayList<>();

    private List<String> roles = new ArrayList<>();

    private boolean permitAll;
}
