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
package org.wildfly.swarm.jaxrs.internal;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.undertow.internal.DefaultWarDeploymentFactory;

/**
 * @author Bob McWhirter
 */
public class DefaultJAXRSWarDeploymentFactory extends DefaultWarDeploymentFactory {

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
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class, determineName());
        setup(archive);
        return archive;
    }
}
