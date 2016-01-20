package org.wildfly.swarm.io.runtime;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.extension.io.IOExtension;
import org.wildfly.swarm.container.runtime.AbstractParserFactory;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @since 24/11/15
 */
public class IOParserFactory extends AbstractParserFactory {
    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        IOExtension ext = new IOExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
