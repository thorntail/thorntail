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
package org.wildfly.swarm.topology.internal;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.topology.TopologyArchive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class TopologyArchiveImpl extends AssignableBase<ArchiveBase<?>> implements TopologyArchive {

    public static final String SERVICE_ACTIVATOR_CLASS_NAME = "org.wildfly.swarm.topology.deployment.RegistrationAdvertiserActivator";

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public TopologyArchiveImpl(ArchiveBase<?> archive) throws IOException {
        super(archive);

        Node regConf = as(JARArchive.class).get(REGISTRATION_CONF);
        if (regConf != null && regConf.getAsset() != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(regConf.getAsset().openStream()))) {
                reader.lines().forEach(this::parseConfigLine);
            }
        }
    }

    @Override
    public TopologyArchive advertise() {
        return doAdvertise();
    }

    @Override
    public TopologyArchive advertise(String serviceName) {
        return advertise(serviceName, Collections.emptyList());
    }

    @Override
    public TopologyArchive advertise(String serviceName, Collection<String> tags) {
        tagsByService.put(serviceName, tags);
        return doAdvertise();
    }

    @Override
    public List<String> advertisements() {
        List<String> serviceNames = new ArrayList<>(tagsByService.keySet());
        return Collections.unmodifiableList(serviceNames);
    }

    @Override
    public boolean hasAdvertised() {
        return as(JARArchive.class).get(REGISTRATION_CONF) != null;
    }

    private void parseConfigLine(String line) {
        line = line.trim();
        if (!line.isEmpty()) {
            List<String> split = new ArrayList<>(Arrays.asList(line.split(SERVICE_TAG_SEPARATOR)));
            String serviceName = split.get(0);

            List<String> tags = split.size() > 1
                    ? split.subList(1, split.size())
                    : Collections.emptyList();
            tagsByService.put(serviceName, tags);
        }
    }

    protected List<String> getServiceNames() {
        List<String> result = advertisements();
        if (!result.isEmpty()) {
            return result;
        }
        String archiveName = this.getArchive().getName();
        int lastDotLoc = archiveName.lastIndexOf('.');
        if (lastDotLoc > 0) {
            return Collections.singletonList(archiveName.substring(0, lastDotLoc));
        }
        return Collections.singletonList(archiveName);
    }

    protected TopologyArchive doAdvertise() {
        if (!as(ServiceActivatorArchive.class).containsServiceActivator(SERVICE_ACTIVATOR_CLASS_NAME)) {
            as(ServiceActivatorArchive.class).addServiceActivator(SERVICE_ACTIVATOR_CLASS_NAME);
            //as(JARArchive.class).addModule("org.wildfly.swarm.topology", "deployment");
        }

        StringBuilder registrationConf = new StringBuilder();

        List<String> names = getServiceNames();
        for (String name : names) {
            Collection<String> tags = tagsByService.getOrDefault(name, Collections.emptyList());
            registrationConf.append(name);
            if (!tags.isEmpty()) {
                registrationConf.append(" ")
                        .append(
                                tags.stream().collect(Collectors.joining(TAG_SEPARATOR))
                        );
            }
            registrationConf.append("\n");
        }

        as(JARArchive.class).add(new StringAsset(registrationConf.toString()), REGISTRATION_CONF);
        return this;
    }

    private Map<String, Collection<String>> tagsByService = new HashMap<>();


}
