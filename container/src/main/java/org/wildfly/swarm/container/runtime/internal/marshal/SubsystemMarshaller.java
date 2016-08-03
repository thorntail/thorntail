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
package org.wildfly.swarm.container.runtime.internal.marshal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlySubsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class SubsystemMarshaller implements ConfigurationMarshaller {

    @Inject
    @MarshalDMR
    private Instance<Fraction> dmrFractions;

    @Inject
    @WildFlySubsystem
    private Instance<Fraction> subsystemOnlyFractions;

    public List<ModelNode> marshal() {
        List<ModelNode> list = new ArrayList<>();
        for (Fraction each : this.dmrFractions) {

            MarshalDMR anno = each.getClass().getAnnotation(MarshalDMR.class);

            System.err.println( "annotation: " + anno + " on " + each + " // " + each.getClass() );

            try {
                Marshaller marshaller = new Marshaller();
                LinkedList<ModelNode> subList = marshaller.marshal(each);
                list.addAll(subList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for ( Fraction each : this.subsystemOnlyFractions ) {
            MarshalDMR dmrAnno = each.getClass().getAnnotation(MarshalDMR.class);

            if ( dmrAnno != null ) {
                // already marshalled as full config-api DMR
                continue;
            }

            WildFlySubsystem subsysAnno = each.getClass().getAnnotation(WildFlySubsystem.class);

            PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, subsysAnno.value() ));

            ModelNode node = new ModelNode();
            node.get(OP_ADDR).set(address.toModelNode());
            node.get(OP).set(ADD);
            list.add(node);
        }

        return list;
    }
}
