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
package org.wildfly.swarm.topology.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.topology.TopologyArchive;

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
    public TopologyArchiveImpl(ArchiveBase<?> archive) {
        super(archive);

        Node regConf = as(JARArchive.class).get(REGISTRATION_CONF);
        if ( regConf != null && regConf.getAsset() != null ) {
            try ( BufferedReader reader = new BufferedReader( new InputStreamReader( regConf.getAsset().openStream() ) ) ) {
                reader.lines()
                        .forEach( line->{
                            this.serviceNames.add( line );
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public TopologyArchive advertise() {
        doAdvertise();
        return this;
    }

    @Override
    public TopologyArchive advertise(String... serviceNames) {
        for (String serviceName : serviceNames) {
            this.serviceNames.add(serviceName);
        }

        return advertise();
    }

    @Override
    public TopologyArchive advertise(Collection<String> serviceNames) {
        this.serviceNames.addAll( serviceNames );
        return advertise();
    }

    @Override
    public List<String> advertisements() {
        return Collections.unmodifiableList( this.serviceNames );
    }

    @Override
    public boolean hasAdvertised() {
        return as(JARArchive.class).get(REGISTRATION_CONF) != null;
    }

    protected List<String> getServiceNames() {
        if (!this.serviceNames.isEmpty()) {
            return this.serviceNames;
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
            as(JARArchive.class).addModule("org.wildfly.swarm.topology", "deployment");
        }

        StringBuffer buf = new StringBuffer();

        List<String> names = getServiceNames();
        for (String name : names) {
            buf.append(name).append("\n");
        }

        as(JARArchive.class).add(new StringAsset(buf.toString()), REGISTRATION_CONF);
        return this;
    }

    private List<String> serviceNames = new ArrayList<>();


}
