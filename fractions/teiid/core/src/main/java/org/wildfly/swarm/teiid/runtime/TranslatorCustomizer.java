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
import java.util.Map;
import java.util.ServiceLoader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.Translator;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.teiid.TeiidFraction;

@Pre
@ApplicationScoped
public class TranslatorCustomizer implements Customizer {
    private static final String PRE = "org.jboss.teiid.translator.";

    @Inject
    TeiidFraction fraction;

    @Override
    public void customize() throws Exception {
        HashMap<String, String> ras = new HashMap<>();
        ras.put(PRE + "accumulo", "accumulo");
        ras.put(PRE + "amazon.s3", "amazon-s3");
        ras.put(PRE + "cassandra", "cassandra");
        ras.put(PRE + "couchbase", "couchbase");
        ras.put(PRE + "excel", "excel");
        ras.put(PRE + "file", "file");
        ras.put(PRE + "ftp", "ftp");
        ras.put(PRE + "google", "google");
        ras.put(PRE + "hive", "hive");
        ras.put(PRE + "infinispan-hotrod", "infinispan-hotrod");
        ras.put(PRE + "jdbc", "jdbc");
        ras.put(PRE + "ldap", "ldap");
        ras.put(PRE + "loopback", "loopback");
        ras.put(PRE + "mongodb", "mongodb");
        ras.put(PRE + "odata", "odata");
        ras.put(PRE + "odata4", "odata4");
        ras.put(PRE + "olap", "olap");
        ras.put(PRE + "pheonix", "pheonix");
        ras.put(PRE + "prestodb", "prestodb");
        ras.put(PRE + "salesforce", "salesforce");
        ras.put(PRE + "salesforce-34", "salesforce-34");
        ras.put(PRE + "salesforce-41", "salesforce-41");
        ras.put(PRE + "simpledb", "simpledb");
        ras.put(PRE + "solr", "solr");
        ras.put(PRE + "swagger", "swagger");
        ras.put(PRE + "ws", "ws");

        for (Map.Entry<String, String> e : ras.entrySet()) {
            loadTranslators(e.getKey());
        }
    }

    @SuppressWarnings("rawtypes")
    private void loadTranslators(String moduleName) {
        ClassLoader translatorLoader = this.getClass().getClassLoader();
        try {
            final Module module = Module.getBootModuleLoader().loadModule(moduleName);
            if (module != null) {
                translatorLoader = module.getClassLoader();
                final ServiceLoader<ExecutionFactory> serviceLoader = ServiceLoader.load(ExecutionFactory.class,
                        translatorLoader);
                if (serviceLoader != null) {
                    for (ExecutionFactory ef : serviceLoader) {
                        Translator t = ef.getClass().getAnnotation(Translator.class);
                        fraction.translator(t.name(), x -> x.module(moduleName));
                    }
                }
            }
        } catch (ModuleLoadException e) {
            //no-op
        }
    }
}
