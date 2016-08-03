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
package org.wildfly.swarm.logging.runtime;

import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Vetoed;
import javax.xml.namespace.QName;

import org.jboss.as.logging.LoggingExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;

/**
 * The actual logging parsers are package protected, hence this intermediary.
 *
 * @author Heiko Braun
 * @since 10/11/15
 */
@Vetoed
public class LoggingParserFactory extends AbstractParserFactory {

    public Map<QName, XMLElementReader<List<ModelNode>>> create() {

        ParsingContext ctx = new ParsingContext();
        LoggingExtension ext = new LoggingExtension();
        ext.initializeParsers(ctx);
        return ctx.getParser();
    }
}
