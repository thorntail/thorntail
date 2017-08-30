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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ken Finnigan
 */
public class Servlet {

    public Servlet(String servletName, String servletClass) {
        this.servletName = servletName;
        this.servletClass = servletClass;
    }

    public String servletName() {
        return this.servletName;
    }

    public String servletClass() {
        return this.servletClass;
    }

    public Servlet withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String displayName() {
        return this.displayName;
    }

    public Servlet withDescription(String description) {
        this.description = description;
        return this;
    }

    public String description() {
        return this.description;
    }

    public Servlet withAsyncSupported(boolean async) {
        this.async = async;
        return this;
    }

    public Boolean asyncSupported() {
        return this.async;
    }

    public Servlet withLoadOnStartup(Integer loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
        return this;
    }

    public Integer loadOnStartup() {
        return this.loadOnStartup;
    }

    public Servlet withEnabled(boolean enabled) {
        return null;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public Servlet withInitParam(String name, String value) {
        this.initParams.put(name, value);
        return this;
    }

    public Servlet withInitParams(Map<String, String> params) {
        this.initParams.putAll(params);
        return this;
    }

    public Map<String, String> initParams() {
        return this.initParams;
    }

    public Servlet withUrlPattern(String urlPattern) {
        this.urlPatterns.add(urlPattern);
        return this;
    }

    public Servlet withUrlPatterns(String... urlPatterns) {
        this.urlPatterns.addAll(Arrays.asList(urlPatterns));
        return this;
    }

    public Servlet withUrlPatterns(List<String> urlPatterns) {
        this.urlPatterns.addAll(urlPatterns);
        return this;
    }

    public List<String> urlPatterns() {
        return this.urlPatterns;
    }

    private final String servletName;

    private final String servletClass;

    private String displayName;

    private String description;

    private Boolean async = null;

    private Integer loadOnStartup = null;

    private Boolean enabled = Boolean.TRUE;

    private Map<String, String> initParams = new HashMap<>();

    private List<String> urlPatterns = new ArrayList<>();
}
