package org.wildfly.swarm.container.runtime;

import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Vetoed;
import javax.xml.namespace.QName;

import org.jboss.as.controller.Extension;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class GenericParserFactory extends AbstractParserFactory {

    private final Extension extension;

    public GenericParserFactory(Extension extension) {
        this.extension = extension;
    }

    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        this.extension.initializeParsers(ctx);
        return ctx.getParser();
    }
}
