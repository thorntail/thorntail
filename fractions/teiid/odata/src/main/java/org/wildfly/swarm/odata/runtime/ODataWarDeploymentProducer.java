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
package org.wildfly.swarm.odata.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.odata.ODataFraction;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.undertow.WARArchive;

@Post
@ApplicationScoped
public class ODataWarDeploymentProducer {
    @Inject
    @Any
    ODataFraction fraction;

    @Produces
    public Archive odataWar() throws Exception {
        // TODO: Keycloak Integration for security (see Jolokia project)
        WARArchive war = ShrinkWrap.create(WARArchive.class, "odata.war")
                .setContextRoot(this.fraction.getContext())
                .setWebXML(this.getClass().getResource("/web.xml"));
        war.addModule("org.jboss.teiid.olingo");
        return war;
    }
}
