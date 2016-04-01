package org.wildfly.swarm.infinispan.runtime;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;
import org.jboss.as.clustering.infinispan.subsystem.InfinispanExtension;

/**
 * @author Heiko Braun
 * @since 01/04/16
 */
public class ParserFactory extends AbstractParserFactory  {
    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        InfinispanExtension ext = new InfinispanExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
