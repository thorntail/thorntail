package org.wildfly.swarm.logging.runtime;

import org.jboss.as.logging.LoggingExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.container.runtime.AbstractParserFactory;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * The actual logging parsers are package protected, hence this intermediary.
 *
 * @author Heiko Braun
 * @since 10/11/15
 */
public class LoggingParserFactory extends AbstractParserFactory {

    public Map<QName, XMLElementReader<List<ModelNode>>> create() {

        ParsingContext ctx = new ParsingContext();
        LoggingExtension ext = new LoggingExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
