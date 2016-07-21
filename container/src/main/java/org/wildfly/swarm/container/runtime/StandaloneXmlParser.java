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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Vetoed;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.as.controller.parsing.Namespace;
import org.jboss.as.controller.parsing.ProfileParsingCompletionHandler;
import org.jboss.as.server.parsing.ExtensionHandler;
import org.jboss.as.server.parsing.StandaloneXml;
import org.jboss.as.server.parsing.StandaloneXml.ParsingOption;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;
import org.jboss.staxmapper.XMLMapper;

/**
 * @author Heiko Braun
 * @since 27/11/15
 */
public class StandaloneXmlParser {

    private List<ModelNode> usedExtensions;

    public StandaloneXmlParser() {

        parserDelegate = new StandaloneXml(new ExtensionHandler() {
            @Override
            public void parseExtensions(XMLExtendedStreamReader reader, ModelNode address, Namespace namespace, List<ModelNode> list) throws XMLStreamException {
                reader.discardRemainder(); // noop
            }

            @Override
            public Set<ProfileParsingCompletionHandler> getProfileParsingCompletionHandlers() {
                return Collections.EMPTY_SET;
            }

            @Override
            public void writeExtensions(XMLExtendedStreamWriter writer, ModelNode modelNode) throws XMLStreamException {
                // noop
            }
        }, ParsingOption.IGNORE_SUBSYSTEM_FAILURES);

        xmlMapper = XMLMapper.Factory.create();
        xmlMapper.registerRootElement(new QName("urn:jboss:domain:4.0", "server"), parserDelegate);

    }

    /**
     * Add a parser for a subpart of the XML model.
     *
     * @param elementName the FQ element name (i.e. subsystem name)
     * @param parser      creates ModelNode's from XML input
     * @return
     */
    public StandaloneXmlParser addDelegate(QName elementName, XMLElementReader<List<ModelNode>>  parser) {
        xmlMapper.registerRootElement(elementName, parser);
        return this;
    }

    public List<ModelNode> parse(URL xml) throws Exception {

        final List<ModelNode> operationList = new ArrayList<>();

        InputStream input = null;
        try {
            input = xml.openStream();

            final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(input);

            xmlMapper.parseDocument(operationList, reader);

            //operationList.forEach(System.out::println);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException e) {
                // ignore
            }
        }

        return operationList;
    }

    private final XMLMapper xmlMapper;

    private final StandaloneXml parserDelegate;

    @Vetoed
    private static class NoopXMLElementReader implements XMLElementReader<List<ModelNode>> {
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> modelNode) throws XMLStreamException {
            System.out.println("Skip " + reader.getNamespaceURI() + "::" + reader.getLocalName());
            reader.discardRemainder();
        }
    }

}
