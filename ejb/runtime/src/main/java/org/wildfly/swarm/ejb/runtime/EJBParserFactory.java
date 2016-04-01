package org.wildfly.swarm.ejb.runtime;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.as.ejb3.subsystem.EJB3Extension;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;

/**
 * @author Heiko Braun
 * @since 01/04/16
 */
public class EJBParserFactory extends AbstractParserFactory {

    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        EJB3Extension ext = new EJB3Extension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
