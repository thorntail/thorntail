package org.wildfly.swarm.container.runtime.usage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

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
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
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
