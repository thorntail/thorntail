package org.wildfly.swarm.cdi.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;
import org.wildfly.swarm.spi.runtime.MarshallingServerConfiguration;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Heiko Braun
 * @since 21/04/16
 */
public class CDIConfiguration extends MarshallingServerConfiguration<CDIFraction> {

    public CDIConfiguration() {
        super(CDIFraction.class, "org.jboss.as.weld");
    }

    @Override
    public void prepareArchive(Archive<?> a) {

        if(a.getName().endsWith(".war")) { // TODO: fix this
            try {
                WARArchive warArchive = a.as(WARArchive.class);
                warArchive.addModule("org.wildfly.swarm.spi");
                warArchive.addAsLibraries(Swarm.artifact("org.wildfly.swarm:cdi-ext:jar:"+VERSION));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public Optional<Map<QName, XMLElementReader<List<ModelNode>>>> getSubsystemParsers() throws Exception {
        return AbstractParserFactory.mapParserNamespaces(new ParserFactory());
    }

    public static final String VERSION;

    static {
        InputStream in = CDIFraction.class.getClassLoader().getResourceAsStream("cdi-fraction.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VERSION = props.getProperty("version", "unknown");
    }

}
