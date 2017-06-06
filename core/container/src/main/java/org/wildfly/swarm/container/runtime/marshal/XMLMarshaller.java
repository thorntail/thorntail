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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.runtime.xmlconfig.StandaloneXMLParser;
import org.wildfly.swarm.container.runtime.xmlconfig.XMLConfig;
import org.wildfly.swarm.internal.SwarmConfigMessages;
import org.wildfly.swarm.spi.runtime.ConfigurationMarshaller;

/**
 * Marshals a collection of XML configurations (standalone.xml) to DMR.
 *
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

        Set<URL> seen = new HashSet<>();
        xmlConfig.forEach(url -> parse(url, seen, list));
    }

    protected void parse(URL url, Set<URL> seen, List<ModelNode> list) {
        if (url == null) {
            return;
        }
        if (seen.contains(url)) {
            return;
        }
        seen.add(url);
        try {
            List<ModelNode> subList = this.parser.parse(url);
            SwarmConfigMessages.MESSAGES.marshalXml(url.toExternalForm(), subList.toString());
            list.addAll(subList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
