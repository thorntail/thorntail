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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlySubsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
@Singleton
public class SubsystemMarshaller implements ConfigurationMarshaller {

    @Inject
    @Any
    private Instance<Fraction> fractions;

    public void marshal(List<ModelNode> list) {
        for (Fraction each : this.fractions) {

            MarshalDMR anno = each.getClass().getAnnotation(MarshalDMR.class);

            if (anno != null) {
                try {
                    LinkedList<ModelNode> subList = Marshaller.marshal(each);
                    if (!isAlreadyConfigured(subList, list)) {
                        list.addAll(subList);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                WildFlySubsystem subsysAnno = each.getClass().getAnnotation(WildFlySubsystem.class);

                if (subsysAnno != null) {

                    PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, subsysAnno.value()));

                    ModelNode node = new ModelNode();
                    node.get(OP_ADDR).set(address.toModelNode());
                    node.get(OP).set(ADD);
                    List<ModelNode> subList = Collections.singletonList(node);

                    if (!isAlreadyConfigured(subList, list)) {
                        list.addAll(subList);
                    }
                }
            }
        }

    }
}
