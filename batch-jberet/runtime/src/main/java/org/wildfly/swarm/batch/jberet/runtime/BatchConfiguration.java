/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.batch.jberet.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.batch.jberet.BatchFraction;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class BatchConfiguration extends AbstractServerConfiguration<BatchFraction> {
    public BatchConfiguration() {
        super(BatchFraction.class);
    }

    @Override
    public BatchFraction defaultFraction() {
        return BatchFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(final BatchFraction fraction) throws Exception {
        final List<ModelNode> list = new ArrayList<>();
        list.add(Operations.createAddOperation(Operations.createAddress(EXTENSION, "org.wildfly.extension.batch.jberet")));
        list.addAll(Marshaller.marshal(fraction));
        return list;
    }
}
