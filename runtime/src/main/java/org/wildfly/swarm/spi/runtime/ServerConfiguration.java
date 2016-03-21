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
package org.wildfly.swarm.spi.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.jandex.Index;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
public interface ServerConfiguration<T extends Fraction> {

    Class<T> getType();

    T defaultFraction();

    default List<ServiceActivator> getServiceActivators(T fraction) {
        return Collections.emptyList();
    }

    default void prepareArchive(Archive<?> a) {

    }

    default void processArchiveMetaData(Archive<?> a, Index idx) {

    }

    default List<Archive> getImplicitDeployments(T fraction, ArtifactLookup lookup) throws Exception {
        return getImplicitDeployments( fraction );
    }

    default List<Archive> getImplicitDeployments(T fraction) throws Exception {
        return Collections.emptyList();
    }

    default List<ModelNode> getList(T fraction) throws Exception {
        return Collections.emptyList();
    }

    default Optional<Map<QName, XMLElementReader<List<ModelNode>>>> getSubsystemParsers() throws Exception {
        return Optional.empty();
    }

    default boolean isIgnorable() {
        return false;
    }

    default Optional<ModelNode> getExtension() {
        return Optional.empty();
    }
}
