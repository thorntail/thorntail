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
package org.wildfly.swarm.container.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.Fraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public abstract class ExtensionServerConfiguration<T extends Fraction> extends AbstractServerConfiguration<T> {

    public ExtensionServerConfiguration(Class<T> type, String extensionModuleName) {
        super(type);
        this.extensionModuleName = extensionModuleName;
    }

    public String getExtensionModuleName() {
        return this.extensionModuleName;
    }

    @Override
    public Optional<ModelNode> getExtension() {
        if (this.extensionModuleName != null) {
            ModelNode node = new ModelNode();
            node.get(OP_ADDR).set(EXTENSION, this.extensionModuleName);
            node.get(OP).set(ADD);
            return Optional.of(node);
        }

        return Optional.empty();
    }

    @Override
    public List<ModelNode> getList(T fraction) throws Exception {
        return new ArrayList<>();
    }

    private final String extensionModuleName;
}
