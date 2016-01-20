package org.wildfly.swarm.undertow.runtime;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.extension.undertow.UndertowExtension;
import org.wildfly.swarm.container.runtime.AbstractParserFactory;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @since 24/11/15
 */
public class UndertowParserFactory extends AbstractParserFactory {

    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        UndertowExtension ext = new UndertowExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
