package org.wildfly.swarm.mail.runtime;


import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.as.mail.extension.MailExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;

/**
 * @author Heiko Braun
 * @since 01/04/16
 */
public class ParserFactory extends AbstractParserFactory  {
    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        MailExtension ext = new MailExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
