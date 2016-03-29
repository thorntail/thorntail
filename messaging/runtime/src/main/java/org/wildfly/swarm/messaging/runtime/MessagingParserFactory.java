package org.wildfly.swarm.messaging.runtime;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.extension.messaging.activemq.MessagingExtension;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;

/**
 * @author Heiko Braun
 * @since 29/03/16
 */
public class MessagingParserFactory extends AbstractParserFactory {
    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        MessagingExtension ext = new MessagingExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
