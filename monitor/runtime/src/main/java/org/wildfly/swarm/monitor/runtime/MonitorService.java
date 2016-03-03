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
package org.wildfly.swarm.monitor.runtime;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;

//import org.wildfly.swarm.container.Container;

/**
 * @author Heiko Braun
 * @since 19/02/16
 */
public class MonitorService implements Monitor, Service<MonitorService> {

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "monitor");

    @Override
    public void start(StartContext startContext) throws StartException {
        System.out.println("<< Starting monitor service >>");
        executorService = Executors.newSingleThreadExecutor();
        serverEnvironment = serverEnvironmentValue.getValue();
        controllerClient = modelControllerValue.getValue().createClient(executorService);
    }

    @Override
    public void stop(StopContext stopContext) {
        System.out.println("<< Stopping monitor service >>");
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Override
    public MonitorService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public ModelNode getNodeInfo() {
        ModelNode payload = new ModelNode();


        ModelNode op = new ModelNode();
        op.get(ADDRESS).setEmptyList();
        op.get(OP).set("query");
        op.get("select").add("name");
        op.get("select").add("server-state");
        op.get("select").add("suspend-state");
        op.get("select").add("running-mode");
        op.get("select").add("uuid");

        try {
            ModelNode response = controllerClient.execute(op);
            ModelNode unwrapped = unwrap(response);
            // need a way to figure out *which* version we really mean here...
            unwrapped.get("wfs-version").set( "fixme" );
            return unwrapped;
        } catch (IOException e) {
            return new ModelNode().get(FAILURE_DESCRIPTION).set(e.getMessage());
        }

    }

    @Override
    public ModelNode heap() {

        // /core-service=platform-mbean/type=memory:read-resource(include-runtime=true)

        ModelNode op = new ModelNode();
        op.get(ADDRESS).add("core-service", "platform-mbean");
        op.get(ADDRESS).add("type", "memory");
        op.get(OP).set("query");
        op.get("select").add("heap-memory-usage");
        op.get("select").add("non-heap-memory-usage");

        try {
            ModelNode response = controllerClient.execute(op);
            return unwrap(response);
        } catch (IOException e) {
            return new ModelNode().get(FAILURE_DESCRIPTION).set(e.getMessage());
        }
    }

    @Override
    public ModelNode threads() {

        // /core-service=platform-mbean/type=threading:read-resource(include-runtime=true)

        ModelNode op = new ModelNode();
        op.get(ADDRESS).add("core-service", "platform-mbean");
        op.get(ADDRESS).add("type", "threading");
        op.get(OP).set("query");
        op.get("select").add("thread-count");
        op.get("select").add("peak-thread-count");
        op.get("select").add("total-started-thread-count");
        op.get("select").add("current-thread-cpu-time");
        op.get("select").add("current-thread-user-time");

        try {
            ModelNode response = controllerClient.execute(op);
            return unwrap(response);
        } catch (IOException e) {
            return new ModelNode().get(FAILURE_DESCRIPTION).set(e.getMessage());
        }
    }

    private static ModelNode unwrap(ModelNode response) {
        if (response.get(OUTCOME).asString().equals(SUCCESS))
            return response.get(RESULT);
        else
            return response;
    }

    final InjectedValue<ServerEnvironment> serverEnvironmentValue = new InjectedValue<ServerEnvironment>();

    final InjectedValue<ModelController> modelControllerValue = new InjectedValue<ModelController>();

    private ExecutorService executorService;

    private ServerEnvironment serverEnvironment;

    private ModelControllerClient controllerClient;
}
