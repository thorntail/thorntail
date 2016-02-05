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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.jboss.as.controller.ProcessType;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ProfileParsingCompletionHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;

/**
 * @author Heiko Braun
 * @since 23/11/15
 */
public abstract class AbstractParserFactory {

    private static final String SUBSYSTEM = "subsystem";

    /**
     * Parsers retain the namespace, but the local part becomes 'subsystem'
     *
     * @param factory the factory producing the parsers
     * @return
     */
    public static Optional<Map<QName, XMLElementReader<List<ModelNode>>>> mapParserNamespaces(AbstractParserFactory factory) {
        Map<QName, XMLElementReader<List<ModelNode>>> result =
                factory.create().entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                e -> new QName(e.getKey().getNamespaceURI(), SUBSYSTEM),
                                e -> e.getValue()
                        ));

        return Optional.of(result);
    }

    public abstract Map<QName, XMLElementReader<List<ModelNode>>> create();

    public class ParsingContext implements ExtensionParsingContext {

        Map<QName, XMLElementReader<List<ModelNode>>> parsers = new HashMap<>();

        public Map<QName, XMLElementReader<List<ModelNode>>> getParser() {
            return parsers;
        }

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
            parsers.put(new QName(namespace, localName), parser);

        }

        @Override
        public void setProfileParsingCompletionHandler(ProfileParsingCompletionHandler profileParsingCompletionHandler) {
            // ignore
        }
    }
}
