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
package org.wildfly.swarm.container.runtime.marshal;

import java.net.URL;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.runtime.xmlconfig.StandaloneXMLParser;
import org.wildfly.swarm.container.runtime.xmlconfig.XMLConfig;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class XMLMarshaller implements ConfigurationMarshaller {

    @Inject
    @XMLConfig
    private Instance<URL> xmlConfig;

    @Inject
    private StandaloneXMLParser parser;

    public void marshal(List<ModelNode> list) {
        if (this.xmlConfig.isUnsatisfied()) {
            return;
        }

        xmlConfig.forEach( url-> parse(url, list));
    }

    protected void parse(URL url, List<ModelNode> list) {
        System.err.println( "marshal XML: " + url );
        if ( url == null ) {
            return;
        }
        try {
            List<ModelNode> subList = this.parser.parse(url);
            System.err.println( ">>> xml" );
            System.err.println( subList );
            System.err.println( "<<< xml" );
            list.addAll( subList );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
