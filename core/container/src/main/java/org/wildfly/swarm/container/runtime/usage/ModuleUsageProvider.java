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
package org.wildfly.swarm.container.runtime.usage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.Module;

/**
 * Created by bob on 8/30/17.
 */
@ApplicationScoped
public class ModuleUsageProvider implements UsageProvider {

    String USAGE_TXT = "usage.txt";

    String META_INF_USAGE_TXT = "META-INF/" + USAGE_TXT;

    String WEB_INF_USAGE_TXT = "WEB-INF/" + USAGE_TXT;

    @Override
    public String getRawUsageText() throws Exception {
        Module module = Module.getBootModuleLoader().loadModule("thorntail.application");
        ClassLoader cl = module.getClassLoader();

        InputStream in = cl.getResourceAsStream(META_INF_USAGE_TXT);

        if (in == null) {
            in = cl.getResourceAsStream(WEB_INF_USAGE_TXT);
        }

        if (in == null) {
            in = cl.getResourceAsStream(USAGE_TXT);
        }

        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader
                        .lines()
                        .collect(Collectors.joining("\n"));
            }
        }

        return null;
    }
}
