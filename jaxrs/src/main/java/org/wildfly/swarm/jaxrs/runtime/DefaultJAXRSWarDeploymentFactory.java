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
package org.wildfly.swarm.jaxrs.runtime;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.bootstrap.env.NativeDeploymentFactory;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.undertow.runtime.DefaultWarDeploymentFactory;

/**
 * @author Bob McWhirter
 */
@Singleton
public class DefaultJAXRSWarDeploymentFactory extends DefaultWarDeploymentFactory {

    @Inject
    public DefaultJAXRSWarDeploymentFactory(NativeDeploymentFactory nativeDeploymentFactory) {
        super( nativeDeploymentFactory );
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public String getType() {
        return "war";
    }

    @Override
    public Archive create() throws Exception {
        return super.create().as(JAXRSArchive.class);
    }

    @Override
    public Archive createFromJar() throws Exception {
        return super.createFromJar().as(JAXRSArchive.class);
    }
}
