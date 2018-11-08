/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.JDKModuleFinder;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoader;

/**
 * @author Bob McWhirter
 */
public class BootModuleLoader extends ModuleLoader {

    public BootModuleLoader() {
        super(new ModuleFinder[]{
                JDKModuleFinder.getInstance(),
                new BootstrapClasspathModuleFinder(),
                new BootstrapModuleFinder(),
                new ClasspathModuleFinder(),
                new ContainerModuleFinder(),
                new ApplicationModuleFinder(),
                new DynamicModuleFinder(),
        });
    }
}
