package org.wildfly.swarm.container.runtime.xmlconfig;

import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.jboss.staxmapper.XMLMapper;
import org.wildfly.swarm.internal.SwarmMessages;

/**
 * @author Bob McWhirter
 */
public class WrappedXMLStreamReader implements XMLStreamReader {
    private final XMLStreamReader delegate;

    private final Set<QName> recognizedNames;

    private final XMLMapper mapper;

    public WrappedXMLStreamReader(XMLStreamReader delegate, Set<QName> recognizedNames, XMLMapper mapper) {
        this.delegate = delegate;
        this.recognizedNames = recognizedNames;
        this.mapper = mapper;
    }

    @Override
    public QName getName() {
        QName name = delegate.getName();
        if (!this.recognizedNames.contains(name)) {
            mapper.registerRootElement(name, (reader, value) -> {
                SwarmMessages.MESSAGES.ignoringSubsystem(name.getNamespaceURI(), name.getLocalPart());
                int closesNeeded = 1;

                while (closesNeeded > 0) {
                    int nextTag = reader.next();
                    switch (nextTag) {
                        case XMLEvent.START_ELEMENT:
                            ++closesNeeded;
                            break;
                        case XMLEvent.END_ELEMENT:
                            --closesNeeded;
                            break;
                        default:
                            break;
                    }
                }
            });

        }
        return name;
    }


    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return delegate.getProperty(name);
    }

    @Override
    public int next() throws XMLStreamException {
        return delegate.next();
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        delegate.require(type, namespaceURI, localName);
    }

    @Override
    public String getElementText() throws XMLStreamException {
        return delegate.getElementText();
    }

    @Override
    public int nextTag() throws XMLStreamException {
        return delegate.nextTag();
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return delegate.hasNext();
    }

    @Override
    public void close() throws XMLStreamException {
        delegate.close();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return delegate.getNamespaceURI(prefix);
    }

    @Override
    public boolean isStartElement() {
        return delegate.isStartElement();
    }

    @Override
    public boolean isEndElement() {
        return delegate.isEndElement();
    }

    @Override
    public boolean isCharacters() {
        return delegate.isCharacters();
    }

    @Override
    public boolean isWhiteSpace() {
        return delegate.isWhiteSpace();
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        return delegate.getAttributeValue(namespaceURI, localName);
    }

    @Override
    public int getAttributeCount() {
        return delegate.getAttributeCount();
    }

    @Override
    public QName getAttributeName(int index) {
        return delegate.getAttributeName(index);
    }

    @Override
    public String getAttributeNamespace(int index) {
        return delegate.getAttributeNamespace(index);
    }

    @Override
    public String getAttributeLocalName(int index) {
        return delegate.getAttributeLocalName(index);
    }

    @Override
    public String getAttributePrefix(int index) {
        return delegate.getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(int index) {
        return delegate.getAttributeType(index);
    }

    @Override
    public String getAttributeValue(int index) {
        return delegate.getAttributeValue(index);
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        return delegate.isAttributeSpecified(index);
    }

    @Override
    public int getNamespaceCount() {
        return delegate.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(int index) {
        return delegate.getNamespacePrefix(index);
    }

    @Override
    public String getNamespaceURI(int index) {
        return delegate.getNamespaceURI(index);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    @Override
    public int getEventType() {
        return delegate.getEventType();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public char[] getTextCharacters() {
        return delegate.getTextCharacters();
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return delegate.getTextCharacters(sourceStart, target, targetStart, length);
    }

    @Override
    public int getTextStart() {
        return delegate.getTextStart();
    }

    @Override
    public int getTextLength() {
        return delegate.getTextLength();
    }

    @Override
    public String getEncoding() {
        return delegate.getEncoding();
    }

    @Override
    public boolean hasText() {
        return delegate.hasText();
    }

    @Override
    public Location getLocation() {
        return delegate.getLocation();
    }

    @Override
    public String getLocalName() {
        return delegate.getLocalName();
    }

    @Override
    public boolean hasName() {
        return delegate.hasName();
    }

    @Override
    public String getNamespaceURI() {
        return delegate.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return delegate.getPrefix();
    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    @Override
    public boolean isStandalone() {
        return delegate.isStandalone();
    }

    @Override
    public boolean standaloneSet() {
        return delegate.standaloneSet();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return delegate.getCharacterEncodingScheme();
    }

    @Override
    public String getPITarget() {
        return delegate.getPITarget();
    }

    @Override
    public String getPIData() {
        return delegate.getPIData();
    }
}
