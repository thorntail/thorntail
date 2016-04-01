package org.wildfly.swarm.batch.jberet.runtime;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.extension.batch.jberet.BatchSubsystemExtension;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;

/**
 * @author Heiko Braun
 * @since 01/04/16
 */
public class BatchParserFactory extends AbstractParserFactory {
    @Override
    public Map<QName, XMLElementReader<List<ModelNode>>> create() {
        ParsingContext ctx = new ParsingContext();
        BatchSubsystemExtension ext = new BatchSubsystemExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}