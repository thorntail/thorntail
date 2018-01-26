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
package org.wildfly.swarm.jose.runtime;

import java.lang.reflect.Method;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.modules.Module;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseFraction;
import org.wildfly.swarm.spi.api.UserSpaceExtensionFactory;

/**
 *
 */
@ApplicationScoped
public class InjectJoseExtensionFactory implements UserSpaceExtensionFactory {

    @Inject
    Instance<JoseFraction> joseFractionInstance;

    @Override
    public void configure() throws Exception {
        Module module = Module.getBootModuleLoader().loadModule("org.wildfly.swarm.jose:deployment");
        Class<?> use = module.getClassLoader().loadClass("org.wildfly.swarm.jose.deployment.InjectJoseExtension");
        Method setJose = use.getDeclaredMethod("setJose", Jose.class);
        setJose.invoke(null, this.joseFractionInstance.get().getJoseInstance());
    }

}
