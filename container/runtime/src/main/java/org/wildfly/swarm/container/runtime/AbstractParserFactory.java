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
