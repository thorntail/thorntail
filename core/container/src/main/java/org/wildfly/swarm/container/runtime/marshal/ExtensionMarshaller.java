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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
@Singleton
public class ExtensionMarshaller implements ConfigurationMarshaller {

    @Inject
    @Any
    private Instance<Fraction> fractions;

    public void marshal(List<ModelNode> list) {

        List<ModelNode> extensions = new ArrayList<>();

        Set<String> seen = new HashSet<>();

        for (Fraction each : this.fractions) {
            WildFlyExtension anno = each.getClass().getAnnotation(WildFlyExtension.class);

            if (anno != null) {
                if (anno.module() != null && !anno.module().equals("")) {
                    String module = anno.module();
                    if (!seen.contains(module)) {
                        ModelNode node = new ModelNode();
                        node.get(OP_ADDR).set(EXTENSION, anno.module());
                        node.get(OP).set(ADD);
                        extensions.add(node);
                        seen.add(module);
                    }
                }
            }
        }

        list.addAll(0, extensions);
    }

}
