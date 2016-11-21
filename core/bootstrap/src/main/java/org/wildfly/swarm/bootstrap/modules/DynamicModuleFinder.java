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

package org.wildfly.swarm.bootstrap.modules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;

/** A <code>ModuleFinder</code> which can delegate to dynamically-registerd <code>ModuleFinder</code> instances.
 *
 * @author Bob McWhirter
 */
public class DynamicModuleFinder implements ModuleFinder {

    private static Map<ModuleIdentifier, ModuleFinder> FINDERS = new HashMap<>();

    public static void register(ModuleIdentifier identifier, ModuleFinder finder) {
        FINDERS.put(identifier, finder);
    }

    @Override
    public ModuleSpec findModule(ModuleIdentifier moduleIdentifier, ModuleLoader moduleLoader) throws ModuleLoadException {
        ModuleFinder delegate = FINDERS.get(moduleIdentifier);
        if (delegate == null) {
            return null;
        }

        return delegate.findModule(moduleIdentifier, moduleLoader);
    }
}
