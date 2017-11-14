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
package org.wildfly.swarm.teiid.runtime;

import java.util.HashMap;
import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.resource.adapters.ResourceAdapterFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

@Pre
@ApplicationScoped
public class ResourceAdapterCustomizer implements Customizer {
    private static final String RAPRE = "org.jboss.teiid.resource-adapter.";
    @Inject
    private Instance<ResourceAdapterFraction> fraction;

    @Override
    public void customize() throws Exception {
        // TODO: once WF swarm moves WF 11, we will come with better way like translators for discovery
        HashMap<String, String> ras = new HashMap<>();
        ras.put(RAPRE + "file", "file");
        ras.put(RAPRE + "ftp", "ftp");
        ras.put(RAPRE + "google", "google");
        ras.put(RAPRE + "ldap", "ldap");
        ras.put(RAPRE + "salesforce", "salesforce");
        ras.put(RAPRE + "salesforce-34", "salesforce-34");
        ras.put(RAPRE + "webservice", "webservice");
        ras.put(RAPRE + "mongodb", "mongodb");
        ras.put(RAPRE + "cassandra", "cassandra");
        ras.put(RAPRE + "simpledb", "simpledb");
        ras.put(RAPRE + "accumulo", "accumulo");
        ras.put(RAPRE + "solr", "solr");
        ras.put(RAPRE + "swagger", "swagger");

        Iterator<ModuleIdentifier> it = Module.getBootModuleLoader()
                .iterateModules(ModuleIdentifier.create("org.jboss.teiid.resource-adapter"), false);
        while (it.hasNext()) {
            ModuleIdentifier id = it.next();
            loadResourceAdapter(id, ras);
        }
    }

    private void loadResourceAdapter(ModuleIdentifier id, HashMap<String, String> ras) throws ModuleLoadException {
        final Module module = Module.getBootModuleLoader().loadModule(id);
        if (module != null) {
            String name = ras.get(id.getName());
            fraction.get().resourceAdapter(name, ra -> ra.module(id.getName()));
        }
    }
}
