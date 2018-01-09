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

package org.wildfly.swarm.microprofile.openapi.runtime;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASConfig;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * Accessor to OpenAPI configuration options.
 *
 * Reference:  https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#31-list-of-configurable-items
 *
 * @author eric.wittmann@gmail.com
 *
 *
 *
    mp.openapi.model.reader
    mp.openapi.filter
    mp.openapi.scan.disable
    mp.openapi.scan.packages
    mp.openapi.scan.classes
    mp.openapi.scan.exclude.packages
    mp.openapi.scan.exclude.classes
    mp.openapi.servers
    mp.openapi.servers.path.
    mp.openapi.servers.operation.
 *
 */
@DeploymentScoped
public class OpenApiConfig {

    /**
     * @return the MP config instance
     */
    protected Config getConfig() {
        return ConfigProvider.getConfig();
    }

    public String modelReader() {
        return getConfig().getOptionalValue(OASConfig.MODEL_READER, String.class).orElse(null);
    }

    public String filter() {
        return getConfig().getOptionalValue(OASConfig.FILTER, String.class).orElse(null);
    }

    public boolean scanDisable() {
        return getConfig().getOptionalValue(OASConfig.SCAN_DISABLE, Boolean.class).orElse(false);
    }

    public Set<String> scanPackages() {
        String packages = getConfig().getOptionalValue(OASConfig.SCAN_PACKAGES, String.class).orElse(null);
        return asCsvSet(packages);
    }

    public Set<String> scanClasses() {
        String classes = getConfig().getOptionalValue(OASConfig.SCAN_CLASSES, String.class).orElse(null);
        return asCsvSet(classes);
    }

    public Set<String> scanExcludePackages() {
        String packages = getConfig().getOptionalValue(OASConfig.SCAN_EXCLUDE_PACKAGES, String.class).orElse(null);
        return asCsvSet(packages);
    }

    public Set<String> scanExcludeClasses() {
        String classes = getConfig().getOptionalValue(OASConfig.SCAN_EXCLUDE_CLASSES, String.class).orElse(null);
        return asCsvSet(classes);
    }

    public Set<String> servers() {
        String servers = getConfig().getOptionalValue(OASConfig.SERVERS, String.class).orElse(null);
        return asCsvSet(servers);
    }

    public Set<String> pathServers(String path) {
        String servers = getConfig().getOptionalValue(OASConfig.SERVERS_PATH_PREFIX + path, String.class).orElse(null);
        return asCsvSet(servers);
    }

    public Set<String> operationServers(String operationId) {
        String servers = getConfig().getOptionalValue(OASConfig.SERVERS_OPERATION_PREFIX + operationId, String.class).orElse(null);
        return asCsvSet(servers);
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
