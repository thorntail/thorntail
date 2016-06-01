/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.Module;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;
import org.wildfly.swarm.spi.runtime.MarshallingServerConfiguration;

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
        Module module = a.as(JARArchive.class).addModule("org.wildfly.swarm.cdi", "ext");
        module.withExport(false);
        module.withMetaInf("import");
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
