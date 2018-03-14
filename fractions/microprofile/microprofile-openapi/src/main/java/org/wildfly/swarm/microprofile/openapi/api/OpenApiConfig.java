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

package org.wildfly.swarm.microprofile.openapi.api;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.OASConfig;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;

/**
 * Accessor to OpenAPI configuration options.
 *
 * Reference:  https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#31-list-of-configurable-items
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApiConfig {

    private Config config;

    private String modelReader;
    private String filter;
    private Boolean scanDisable;
    private Set<String> scanPackages;
    private Set<String> scanClasses;
    private Set<String> scanExcludePackages;
    private Set<String> scanExcludeClasses;
    private Set<String> servers;
    private Boolean scanDependenciesDisable;
    private Set<String> scanDependenciesJars;

    public OpenApiConfig(Config config) {
        this.config = config;
    }

    /**
     * @return the MP config instance
     */
    protected Config getConfig() {
        // We cannot use ConfigProvider.getConfig() as the archive is not deployed yet - TCCL cannot be set
        return config;
    }

    public String modelReader() {
        if (modelReader == null) {
            modelReader = getConfig().getOptionalValue(OASConfig.MODEL_READER, String.class).orElse(null);
        }
        return modelReader;
    }

    public String filter() {
        if (filter == null) {
            filter = getConfig().getOptionalValue(OASConfig.FILTER, String.class).orElse(null);
        }
        return filter;
    }

    public boolean scanDisable() {
        if (scanDisable == null) {
            scanDisable = getConfig().getOptionalValue(OASConfig.SCAN_DISABLE, Boolean.class).orElse(false);
        }
        return scanDisable;
    }

    public Set<String> scanPackages() {
        if (scanPackages == null) {
            String packages = getConfig().getOptionalValue(OASConfig.SCAN_PACKAGES, String.class).orElse(null);
            scanPackages = asCsvSet(packages);
        }
        return scanPackages;
    }

    public Set<String> scanClasses() {
        if (scanClasses == null) {
            String classes = getConfig().getOptionalValue(OASConfig.SCAN_CLASSES, String.class).orElse(null);
            scanClasses = asCsvSet(classes);
        }
        return scanClasses;
    }

    public Set<String> scanExcludePackages() {
        if (scanExcludePackages == null) {
            String packages = getConfig().getOptionalValue(OASConfig.SCAN_EXCLUDE_PACKAGES, String.class).orElse(null);
            scanExcludePackages = asCsvSet(packages);
        }
        return scanExcludePackages;
    }

    public Set<String> scanExcludeClasses() {
        if (scanExcludeClasses == null) {
            String classes = getConfig().getOptionalValue(OASConfig.SCAN_EXCLUDE_CLASSES, String.class).orElse(null);
            scanExcludeClasses = asCsvSet(classes);
        }
        return scanExcludeClasses;
    }

    public Set<String> servers() {
        if (servers == null) {
            String theServers = getConfig().getOptionalValue(OASConfig.SERVERS, String.class).orElse(null);
            servers = asCsvSet(theServers);
        }
        return servers;
    }

    public Set<String> pathServers(String path) {
        String servers = getConfig().getOptionalValue(OASConfig.SERVERS_PATH_PREFIX + path, String.class).orElse(null);
        return asCsvSet(servers);
    }

    public Set<String> operationServers(String operationId) {
        String servers = getConfig().getOptionalValue(OASConfig.SERVERS_OPERATION_PREFIX + operationId, String.class).orElse(null);
        return asCsvSet(servers);
    }

    public boolean scanDependenciesDisable() {
        if (scanDependenciesDisable == null) {
            scanDependenciesDisable = getConfig().getOptionalValue(OpenApiConstants.SCAN_DEPENDENCIES_DISABLE, Boolean.class).orElse(false);
        }
        return scanDependenciesDisable;
    }

    public Set<String> scanDependenciesJars() {
        if (scanDependenciesJars == null) {
            String classes = getConfig().getOptionalValue(OpenApiConstants.SCAN_DEPENDENCIES_JARS, String.class).orElse(null);
            scanDependenciesJars = asCsvSet(classes);
        }
        return scanDependenciesJars;
    }


    private static Set<String> asCsvSet(String items) {
        Set<String> rval = new HashSet<>();
        if (items != null) {
            String[] split = items.split(",");
            for (String item : split) {
                rval.add(item.trim());
            }
        }
        return rval;
    }

}
