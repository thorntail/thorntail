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
