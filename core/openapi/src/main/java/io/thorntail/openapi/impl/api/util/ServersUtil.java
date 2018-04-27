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

package io.thorntail.openapi.impl.api.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.thorntail.openapi.impl.api.models.servers.ServerImpl;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.servers.Server;

/**
 * Used to configure server information from config properties.
 *
 * @author eric.wittmann@gmail.com
 * @author Ken Finnigan
 */
@ApplicationScoped
public class ServersUtil {

    @Inject
    private Config config;

    @Inject
    @ConfigProperty(name = OASConfig.SERVERS)
    private Optional<Set<String>> servers;

    public void configureServers(OpenAPI oai) {
        // Start with the global servers.
        if (this.servers.isPresent() && !this.servers.get().isEmpty()) {
            oai.servers(new ArrayList<>());
            for (String server : this.servers.get()) {
                Server s = new ServerImpl();
                s.setUrl(server);
                oai.addServer(s);
            }
        }

        // Now the PathItem and Operation servers
        Set<String> pathNames = oai.getPaths().keySet();
        for (String pathName : pathNames) {
            PathItem pathItem = oai.getPaths().get(pathName);
            configureServers(pathName, pathItem);
        }
    }

    /**
     * Configures the servers for a PathItem.
     *
     * @param pathName
     * @param pathItem
     */
    private void configureServers(String pathName, PathItem pathItem) {
        if (pathItem == null) {
            return;
        }

        Set<String> pathServers = pathServers(pathName);
        if (pathServers != null && !pathServers.isEmpty()) {
            pathItem.servers(new ArrayList<>());
            for (String pathServer : pathServers) {
                Server server = new ServerImpl();
                server.setUrl(pathServer);
                pathItem.addServer(server);
            }
        }

        configureServers(pathItem.getGET());
        configureServers(pathItem.getPUT());
        configureServers(pathItem.getPOST());
        configureServers(pathItem.getDELETE());
        configureServers(pathItem.getHEAD());
        configureServers(pathItem.getOPTIONS());
        configureServers(pathItem.getPATCH());
        configureServers(pathItem.getTRACE());
    }

    /**
     * Configures the servers for an Operation.
     *
     * @param operation
     */
    private void configureServers(Operation operation) {
        if (operation == null) {
            return;
        }
        if (operation.getOperationId() == null) {
            return;
        }

        Set<String> operationServers = operationServers(operation.getOperationId());
        if (operationServers != null && !operationServers.isEmpty()) {
            operation.servers(new ArrayList<>());
            for (String operationServer : operationServers) {
                Server server = new ServerImpl();
                server.setUrl(operationServer);
                operation.addServer(server);
            }
        }
    }

    private Set<String> pathServers(String path) {
        String servers = this.config.getOptionalValue(OASConfig.SERVERS_PATH_PREFIX + path, String.class).orElse(null);
        return asCsvSet(servers);
    }

    private Set<String> operationServers(String operationId) {
        String servers = this.config.getOptionalValue(OASConfig.SERVERS_OPERATION_PREFIX + operationId, String.class).orElse(null);
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
