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
package org.wildfly.swarm.security.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthentication;
import org.wildfly.swarm.config.security.security_domain.authentication.LoginModule;
import org.wildfly.swarm.container.runtime.AbstractParserFactory;
import org.wildfly.swarm.container.runtime.MarshallingServerConfiguration;
import org.wildfly.swarm.security.SecurityFraction;

/**
 * @author Bob McWhirter
 */
public class SecurityConfiguration extends MarshallingServerConfiguration<SecurityFraction> {

    public final static String EXTENSION_NAME = "org.jboss.as.security";

    public SecurityConfiguration() {
        super(SecurityFraction.class, EXTENSION_NAME);
    }

    @Override
    public SecurityFraction defaultFraction() {
        return new SecurityFraction()
                .securityDomain(new SecurityDomain("other")
                                        .classicAuthentication(new ClassicAuthentication()
                                                                       .loginModule(new LoginModule("RealmDirect")
                                                                                            .code("RealmDirect")
                                                                                            .flag(Flag.REQUIRED)
                                                                                            .moduleOptions(new HashMap<Object, Object>() {{
                                                                                                put("password-stacking", "useFirstPass");
                                                                                            }})

                                                                       )));
    }

    @Override
    public List<ModelNode> getList(SecurityFraction fraction) throws Exception {
        if (fraction == null) {
            fraction = defaultFraction();
        }

        List<ModelNode> list = new ArrayList<>();

        ModelNode address = new ModelNode();

        address.setEmptyList();

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }

    @Override
    public Optional<Map<QName, XMLElementReader<List<ModelNode>>>> getSubsystemParsers() throws Exception {
        return AbstractParserFactory.mapParserNamespaces(new SecurityParserFactory());
    }
}
