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
package org.wildfly.swarm.container.runtime.marshal;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.bootstrap.performance.Performance;

import org.wildfly.swarm.spi.runtime.ConfigurationMarshaller;
import org.wildfly.swarm.spi.runtime.CustomMarshaller;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class DMRMarshaller implements ConfigurationMarshaller {

    @Inject
    private XMLMarshaller xmlMarshaller;

    @Inject
    private ConfigViewPropertyMarshaller configViewPropertyMarshaller;

    @Inject
    private ExtensionMarshaller extensionMarshaller;

    @Inject
    private SubsystemMarshaller subsystemMarshaller;

    @Inject
    private Instance<CustomMarshaller> customMarshallers;

    @Inject
    private InterfaceMarshaller interfaceMarshaller;

    @Inject
    private SocketBindingGroupMarshaller socketBindingGroupMarshaller;


    public void marshal(List<ModelNode> list) {
        try {
            try (AutoCloseable handle = Performance.time("marshal XML")) {
                this.xmlMarshaller.marshal(list);
            }
            try (AutoCloseable handle = Performance.time("marshal extensions")) {
                this.extensionMarshaller.marshal(list);
            }
            try (AutoCloseable handle = Performance.time("marshal config-view properties")) {
                this.configViewPropertyMarshaller.marshal(list);
            }
            try (AutoCloseable handle = Performance.time("marshal subsystems")) {
                this.subsystemMarshaller.marshal(list);
            }
            try (AutoCloseable handle = Performance.time("marshal custom")) {
                this.customMarshallers.forEach(e -> e.marshal(list));
            }
            try (AutoCloseable handle = Performance.time("marshal interfaces")) {
                this.interfaceMarshaller.marshal(list);
            }
            try (AutoCloseable handle = Performance.time("marshal socket-bindings")) {
                this.socketBindingGroupMarshaller.marshal(list);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
