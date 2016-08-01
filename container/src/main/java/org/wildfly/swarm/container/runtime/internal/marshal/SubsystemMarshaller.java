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

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class SubsystemMarshaller implements ConfigurationMarshaller {

    @Inject
    @MarshalDMR
    private Instance<Fraction> fractions;


    public List<ModelNode> marshal() {
        List<ModelNode> list = new ArrayList<>();
        for (Fraction each : this.fractions) {

            MarshalDMR anno = each.getClass().getAnnotation(MarshalDMR.class);

            System.err.println( "annotation: " + anno + " on " + each + " // " + each.getClass() );

            if ( anno == null ) {
                // wtf? NPE?
                continue;
            }
            try {
                Marshaller marshaller = new Marshaller();
                LinkedList<ModelNode> subList = marshaller.marshal(each);
                list.addAll(subList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
