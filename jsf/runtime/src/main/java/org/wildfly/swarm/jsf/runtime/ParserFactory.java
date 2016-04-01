package org.wildfly.swarm.jsf.runtime;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.as.jsf.subsystem.JSFExtension;
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
        JSFExtension ext = new JSFExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}