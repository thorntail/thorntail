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
package org.wildfly.swarm.swagger;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.msc.ServiceActivatorArchive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lance Ball
 */
public class SwaggerArchiveImpl extends AssignableBase<ArchiveBase<?>> implements SwaggerArchive {
    public static final String SERVICE_ACTIVATOR_CLASS_NAME = "org.wildfly.swarm.swagger.runtime.SwaggerActivator";
    private List<String> packageNames = new ArrayList<>();

    public SwaggerArchiveImpl(ArchiveBase<?> archive) {
        super(archive);
    }

    @Override
    public SwaggerArchive register(String... packages) {
        for(String name : packages) packageNames.add(name);
        return doRegister();
    }

    private SwaggerArchive doRegister() {
        if (!as(ServiceActivatorArchive.class).containsServiceActivator(SERVICE_ACTIVATOR_CLASS_NAME)) {
            as(ServiceActivatorArchive.class).addServiceActivator(SERVICE_ACTIVATOR_CLASS_NAME);
            as(JARArchive.class).addModule("org.wildfly.swarm.swagger", "runtime");
        }

        StringBuffer buf = new StringBuffer();
        List<String> names = getPackageNames();
        for (String name : names) {
            buf.append(name).append("\n");
        }


        as(JARArchive.class).add(new StringAsset(buf.toString()), SWAGGER_CONFIGURATION_PATH);
        return this;

    }

    public List<String> getPackageNames() {
        return packageNames;
    }
}
