package org.wildfly.swarm.jpa.jpa;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.as.jpa.subsystem.JPAExtension;
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
        JPAExtension ext = new JPAExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}