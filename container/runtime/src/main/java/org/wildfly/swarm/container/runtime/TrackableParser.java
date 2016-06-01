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

import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 * @author Bob McWhirter
 */
public class TrackableParser implements XMLElementReader<List<ModelNode>> {

    private final Optional<ModelNode> extension;
    private final XMLElementReader<List<ModelNode>> delegate;
    private boolean used;

    public TrackableParser(Optional<ModelNode> extension, XMLElementReader<List<ModelNode>> delegate) {
        this.extension = extension;
        this.delegate = delegate;
        this.used = false;
    }

    @Override
    public void readElement(XMLExtendedStreamReader xmlExtendedStreamReader, List<ModelNode> modelNodes) throws XMLStreamException {
        this.delegate.readElement(xmlExtendedStreamReader, modelNodes);
        if ( ! this.used ) {
            this.used = true;
        }
    }

    public boolean isUsed() {
        return this.used;
    }

    public Optional<ModelNode> getExtension() {
        return this.extension;
    }
}
