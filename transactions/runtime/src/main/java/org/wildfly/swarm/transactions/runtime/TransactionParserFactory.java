package org.wildfly.swarm.transactions.runtime;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.as.txn.subsystem.TransactionExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.container.runtime.AbstractParserFactory;

/**
 * @author Heiko Braun
 * @since 23/11/15
 */
public class TransactionParserFactory extends AbstractParserFactory {

    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        TransactionExtension ext = new TransactionExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }


}
