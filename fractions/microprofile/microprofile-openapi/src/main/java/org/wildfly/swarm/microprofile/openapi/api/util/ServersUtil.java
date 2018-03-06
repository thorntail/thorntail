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

package org.wildfly.swarm.microprofile.openapi.api.util;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.wildfly.swarm.microprofile.openapi.api.OpenApiConfig;
import org.wildfly.swarm.microprofile.openapi.api.models.servers.ServerImpl;

/**
 * Used to configure server information from config properties.
 *
 * @author eric.wittmann@gmail.com
 */
public class ServersUtil {

    /**
     * Constructor.
     */
    private ServersUtil() {
    }

    public static final void configureServers(OpenApiConfig config, OpenAPI oai) {
        // Start with the global servers.
        Set<String> servers = config.servers();
        if (servers != null && !servers.isEmpty()) {
            oai.servers(new ArrayList<>());
            for (String server : servers) {
                Server s = new ServerImpl();
                s.setUrl(server);
                oai.addServer(s);
            }
        }

        // Now the PathItem and Operation servers
        Set<String> pathNames = oai.getPaths().keySet();
        for (String pathName : pathNames) {
            PathItem pathItem = oai.getPaths().get(pathName);
            configureServers(config, pathName, pathItem);
        }
    }

    /**
     * Configures the servers for a PathItem.
     * @param config
     * @param pathName
     * @param pathItem
     */
    protected static void configureServers(OpenApiConfig config, String pathName, PathItem pathItem) {
        if (pathItem == null) {
            return;
        }

        Set<String> pathServers = config.pathServers(pathName);
        if (pathServers != null && !pathServers.isEmpty()) {
            pathItem.servers(new ArrayList<>());
            for (String pathServer : pathServers) {
                Server server = new ServerImpl();
                server.setUrl(pathServer);
                pathItem.addServer(server);
            }
        }

        configureServers(config, pathItem.getGET());
        configureServers(config, pathItem.getPUT());
        configureServers(config, pathItem.getPOST());
        configureServers(config, pathItem.getDELETE());
        configureServers(config, pathItem.getHEAD());
        configureServers(config, pathItem.getOPTIONS());
        configureServers(config, pathItem.getPATCH());
        configureServers(config, pathItem.getTRACE());
    }

    /**
     * Configures the servers for an Operation.
     * @param config
     * @param operation
     */
    protected static void configureServers(OpenApiConfig config, Operation operation) {
        if (operation == null) {
            return;
        }
        if (operation.getOperationId() == null) {
            return;
        }

        Set<String> operationServers = config.operationServers(operation.getOperationId());
        if (operationServers != null && !operationServers.isEmpty()) {
            operation.servers(new ArrayList<>());
            for (String operationServer : operationServers) {
                Server server = new ServerImpl();
                server.setUrl(operationServer);
                operation.addServer(server);
            }
        }
    }

}
