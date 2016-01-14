package org.wildfly.swarm.security.runtime;

import org.jboss.as.security.SecurityExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.container.runtime.AbstractParserFactory;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @since 24/11/15
 */
public class SecurityParserFactory extends AbstractParserFactory {
    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        SecurityExtension ext = new SecurityExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
