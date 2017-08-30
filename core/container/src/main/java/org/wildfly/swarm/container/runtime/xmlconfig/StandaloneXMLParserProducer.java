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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ProcessType;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ProfileParsingCompletionHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.internal.SwarmConfigMessages;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * Produces an XML configuration (standalone.xml) parser for available fractions.
 *
 * @author Bob McWhirter
 */
@ApplicationScoped
public class StandaloneXMLParserProducer {

    @Inject
    private Instance<Fraction> fractions;

    private StandaloneXMLParser parser = new StandaloneXMLParser();

    @PostConstruct
    public void setupFactories() throws Exception {
        for (Fraction fraction : this.fractions) {
            setupFactory(fraction);
        }
    }

    @Produces
    @ApplicationScoped
    StandaloneXMLParser standaloneXmlParser() {
        return this.parser;
    }

    private void setupFactory(Fraction fraction) throws Exception {
        try (AutoCloseable handle = Performance.time("Setting up XML parser: " + fraction.getClass().getSimpleName())) {
            WildFlyExtension anno = fraction.getClass().getAnnotation(WildFlyExtension.class);

            if (anno == null) {
                return;
            }

            String extensionModuleName = anno.module();
            String extensionClassName = anno.classname();
            boolean noClass = anno.noClass();

            if (extensionClassName != null && extensionClassName.trim().isEmpty()) {
                extensionClassName = null;
            }

            Module extensionModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(extensionModuleName));

            if (noClass) {
                // ignore it all
            } else if (extensionClassName != null) {
                Class<?> extCls = extensionModule.getClassLoader().loadClass(extensionClassName);
                try {
                    Extension ext = (Extension) extCls.newInstance();
                    add(ext);
                } catch (InstantiationException | IllegalAccessException e) {
                    SwarmConfigMessages.MESSAGES.errorCreatingExtension(extensionClassName, extensionModuleName, e);
                }
            } else {
                ServiceLoader<Extension> extensionLoader = extensionModule.loadService(Extension.class);

                Iterator<Extension> extensionIter = extensionLoader.iterator();
                List<Extension> extensions = new ArrayList<>();

                if (extensionIter.hasNext()) {
                    Extension ext = extensionIter.next();
                    extensions.add(ext);
                }

                if (extensions.size() > 1) {
                    throw SwarmMessages.MESSAGES.fractionHasMultipleExtensions(fraction.getClass().getName(), extensions.stream().map(Objects::toString).collect(Collectors.toList()));
                }

                if (!extensions.isEmpty()) {
                    add(extensions.get(0));
                }
            }
        }
    }

    private void add(Extension ext) {
        ParsingContext ctx = new ParsingContext();
        ext.initializeParsers(ctx);
    }

    class ParsingContext implements ExtensionParsingContext {
        @Override
        public ProcessType getProcessType() {
            return ProcessType.STANDALONE_SERVER;
        }

        @Override
        public RunningMode getRunningMode() {
            return RunningMode.NORMAL;
        }

        @Override
        public void setSubsystemXmlMapping(String localName, String namespace, XMLElementReader<List<ModelNode>> parser) {
            StandaloneXMLParserProducer.this.parser.addDelegate(new QName(namespace, "subsystem"), parser);
        }

        @Override
        public void setProfileParsingCompletionHandler(ProfileParsingCompletionHandler profileParsingCompletionHandler) {
            // ignore
        }
    }

}
