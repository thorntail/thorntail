package org.wildfly.swarm.undertow.runtime;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.extension.undertow.UndertowExtension;
import org.wildfly.swarm.container.runtime.AbstractParserFactory;

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
