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

import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceActivator;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.monitor.MonitorFraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Heiko Braun
 */
public class MonitorConfiguration extends AbstractServerConfiguration<MonitorFraction> {

    public MonitorConfiguration() {
        super(MonitorFraction.class);
    }

    @Override
    public MonitorFraction defaultFraction() {
        return new MonitorFraction();
    }

    @Override
    public List<ModelNode> getList(MonitorFraction fraction) throws Exception {

        /*CustomFilter filter = new CustomFilter("wfs-monitor");
        filter.module("org.wildfly.swarm.monitor.runtime");
        filter.className("org.wildfly.swarm.undertow.runtime.MonitorEndpoints");

        EntityAdapter<CustomFilter> entityAdapter = new EntityAdapter<>(CustomFilter.class);

        ModelNode address = new ModelNode();
        address.get("address").add("subsystem", "undertow");
        address.get("address").add("configuration", "filter");
        address.get("address").add("custom-filter", filter.getKey());

        ModelNode filterResource = entityAdapter.fromEntity(filter, address);

        System.out.println(filterResource);

        List<ModelNode> config = new ArrayList<>(1);
        config.add(filterResource);
        return config;*/
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<ServiceActivator> getServiceActivators(MonitorFraction fraction) {
        List<ServiceActivator> activators = new ArrayList<>();
        activators.add(new MonitorServiceActivator());
        return activators;
    }

}
