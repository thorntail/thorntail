/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.health.runtime;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.inject.Vetoed;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.domain.management.SecurityRealm;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.SwarmInfo;
import org.wildfly.swarm.microprofile.health.HealthMetaData;
import org.wildfly.swarm.microprofile.health.api.Monitor;

/**
 * @author Heiko Braun
 * @since 19/02/16
 */
@Vetoed
public class MonitorService implements Monitor, Service<MonitorService> {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.health");

    private static final String SELECT = "select";

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "health");

    public MonitorService(Optional<String> securityRealm) {
        this.securityRealm = securityRealm;
    }

    @Override
    public long getProbeTimeoutSeconds() {
        return DEFAULT_PROBE_TIMEOUT_SECONDS;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        executorService = Executors.newSingleThreadExecutor();
        serverEnvironment = serverEnvironmentValue.getValue();
        controllerClient = modelControllerValue.getValue().createClient(executorService);

        if (!securityRealm.isPresent()) {
            LOG.warn("You are running the monitoring endpoints without any security realm configuration!");
        }
    }

    @Override
    public void stop(StopContext stopContext) {
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

        ModelNode op = new ModelNode();
        op.get(ADDRESS).setEmptyList();
        op.get(OP).set("query");
        op.get(SELECT).add("name");
        op.get(SELECT).add("server-state");
        op.get(SELECT).add("suspend-state");
        op.get(SELECT).add("running-mode");
        op.get(SELECT).add("uuid");

        try {
            ModelNode response = controllerClient.execute(op);
            ModelNode unwrapped = unwrap(response);
            unwrapped.get("swarm-version").set(SwarmInfo.VERSION);
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
        op.get(SELECT).add("heap-memory-usage");
        op.get(SELECT).add("non-heap-memory-usage");

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
        op.get(SELECT).add("thread-count");
        op.get(SELECT).add("peak-thread-count");
        op.get(SELECT).add("total-started-thread-count");
        op.get(SELECT).add("current-thread-cpu-time");
        op.get(SELECT).add("current-thread-user-time");

        try {
            ModelNode response = controllerClient.execute(op);
            return unwrap(response);
        } catch (IOException e) {
            return new ModelNode().get(FAILURE_DESCRIPTION).set(e.getMessage());
        }
    }

    @Override
    public void registerHealth(HealthMetaData metaData) {
        LOG.info("Adding /health endpoint delegate: " + metaData.getWebContext());
        this.endpoints.add(metaData);
    }

    @Override
    public List<HealthMetaData> getHealthURIs() {
        return Collections.unmodifiableList(this.endpoints);
    }

    @Override
    public Optional<SecurityRealm> getSecurityRealm() {

        if (securityRealm.isPresent() && null == securityRealmServiceValue.getOptionalValue()) {
            throw new RuntimeException("A security realm has been specified, but has not been configured: " + securityRealm.get());
        }

        return securityRealmServiceValue.getOptionalValue() != null ?
                Optional.of(securityRealmServiceValue.getValue()) :
                Optional.empty();

    }

    private static ModelNode unwrap(ModelNode response) {
        if (response.get(OUTCOME).asString().equals(SUCCESS)) {
            return response.get(RESULT);
        } else {
            return response;
        }
    }

    public Injector<ServerEnvironment> getServerEnvironmentInjector() {
        return this.serverEnvironmentValue;
    }

    public Injector<ModelController> getModelControllerInjector() {
        return this.modelControllerValue;
    }

    public Injector<SecurityRealm> getSecurityRealmInjector() {
        return this.securityRealmServiceValue;
    }

    private static final long DEFAULT_PROBE_TIMEOUT_SECONDS = 2;

    private final InjectedValue<ServerEnvironment> serverEnvironmentValue = new InjectedValue<ServerEnvironment>();

    private final InjectedValue<ModelController> modelControllerValue = new InjectedValue<ModelController>();

    private final InjectedValue<SecurityRealm> securityRealmServiceValue = new InjectedValue<SecurityRealm>();

    private final Optional<String> securityRealm;

    private ExecutorService executorService;

    private ServerEnvironment serverEnvironment;

    private ModelControllerClient controllerClient;

    private CopyOnWriteArrayList<HealthMetaData> endpoints = new CopyOnWriteArrayList<HealthMetaData>();

}
