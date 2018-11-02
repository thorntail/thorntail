/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.container.runtime.xmlconfig;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.enterprise.inject.Vetoed;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.as.controller.ProcessType;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.controller.RunningModeControl;
import org.jboss.as.controller.extension.ExtensionRegistry;
import org.jboss.as.controller.extension.RuntimeHostControllerInfoAccessor;
import org.jboss.as.controller.parsing.DeferredExtensionContext;
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
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;

/**
 * @author Heiko Braun
 * @author Ken Finnigan
 * @since 27/11/15
 */
@Vetoed
public class StandaloneXMLParser {

    private static final String SERVER = "server";

    private Set<QName> recognizedNames = new HashSet<>();

    public StandaloneXMLParser() {
        // WF14 this is similar to BootstrapPersister.createDelegate, so should be OK, but better review once more
        ExtensionRegistry extensionRegistry = new ExtensionRegistry(
                ProcessType.SELF_CONTAINED,
                new RunningModeControl(RunningMode.NORMAL),
                null,
                null,
                null,
                RuntimeHostControllerInfoAccessor.SERVER
        );
        DeferredExtensionContext deferredExtensionContext =
            new DeferredExtensionContext(new BootModuleLoader(), extensionRegistry, Executors.newSingleThreadExecutor());
        parserDelegate = new StandaloneXml(
            new ExtensionHandler() {
                @Override
                public void parseExtensions(XMLExtendedStreamReader reader, ModelNode address, Namespace namespace, List<ModelNode> list) throws XMLStreamException {
                    reader.discardRemainder(); // noop
                }

                @Override
                public Set<ProfileParsingCompletionHandler> getProfileParsingCompletionHandlers() {
                    return Collections.emptySet();
                }

                @Override
                public void writeExtensions(XMLExtendedStreamWriter writer, ModelNode modelNode) throws XMLStreamException {
                    // noop
                }
            },
            deferredExtensionContext,
            ParsingOption.IGNORE_SUBSYSTEM_FAILURES);

        xmlMapper = XMLMapper.Factory.create();

        addDelegate(new QName(Namespace.CURRENT.getUriString(), SERVER), parserDelegate);
        addDelegate(new QName("urn:jboss:domain:4.1", SERVER), parserDelegate);
        addDelegate(new QName("urn:jboss:domain:4.0", SERVER), parserDelegate);
        addDelegate(new QName("urn:jboss:domain:2.0", SERVER), parserDelegate);
    }

    /**
     * Add a parser for a subpart of the XML model.
     *
     * @param elementName the FQ element name (i.e. subsystem name)
     * @param parser      creates ModelNode's from XML input
     * @return
     */
    public StandaloneXMLParser addDelegate(QName elementName, XMLElementReader<List<ModelNode>> parser) {
        this.recognizedNames.add(elementName);
        xmlMapper.registerRootElement(elementName, parser);
        return this;
    }

    public StandaloneXMLParser addDelegate(QName elementName, Supplier<XMLElementReader<List<ModelNode>>> supplier) {
        this.recognizedNames.add(elementName);
        xmlMapper.registerRootElement(elementName, supplier.get());
        return this;
    }

    public List<ModelNode> parse(URL xml) throws Exception {
        final List<ModelNode> operationList = new ArrayList<>();

        try (InputStream input = xml.openStream()) {
            final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(input);
            WrappedXMLStreamReader wrappedReader = new WrappedXMLStreamReader(reader, this.recognizedNames, xmlMapper);
            xmlMapper.parseDocument(operationList, wrappedReader);
        }

        return operationList;
    }

    private final XMLMapper xmlMapper;

    private final StandaloneXml parserDelegate;

    @Vetoed
    private static class NoopXMLElementReader implements XMLElementReader<List<ModelNode>> {
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> modelNode) throws XMLStreamException {
            //System.out.println("Skip " + reader.getNamespaceURI() + "::" + reader.getLocalName());
            reader.discardRemainder();
        }
    }

}
