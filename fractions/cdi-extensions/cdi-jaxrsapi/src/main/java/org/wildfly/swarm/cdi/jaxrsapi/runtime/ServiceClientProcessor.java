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
package org.wildfly.swarm.cdi.jaxrsapi.runtime;

import java.util.Collection;

import javax.inject.Inject;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.wildfly.swarm.client.jaxrs.ServiceClient;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * @author Ken Finnigan
 */
@DeploymentScoped
public class ServiceClientProcessor implements DeploymentProcessor {

    @Inject
    public ServiceClientProcessor(Archive archive, IndexView index) {
        this.archive = archive;
        this.index = index;
    }

    @Override
    public void process() {
        Collection<ClassInfo> serviceClients = index.getKnownDirectImplementors((DotName.createSimple(ServiceClient.class.getName())));

        serviceClients.forEach(info -> {
            String name = info.name().toString() + "_generated";
            String path = "WEB-INF/classes/" + name.replace('.', '/') + ".class";
            archive.as(JAXRSArchive.class).add(new ByteArrayAsset(ClientServiceFactory.createImpl(name, info)), path);
        });
    }

    private final Archive<?> archive;

    private final IndexView index;

}
