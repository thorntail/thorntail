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

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSingleModuleFinder implements ModuleFinder {

    public AbstractSingleModuleFinder(String moduleName) {
        this.moduleName = moduleName;
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + this.moduleName + ")";
    }

    @Override
    public ModuleSpec findModule(String identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if (!this.moduleName.equals(identifier)) {
            return null;
        }

        ModuleSpec.Builder builder = ModuleSpec.build(identifier);
        buildModule(builder, delegateLoader);
        return builder.create();
    }

    public abstract void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException;

    private final String moduleName;
}
