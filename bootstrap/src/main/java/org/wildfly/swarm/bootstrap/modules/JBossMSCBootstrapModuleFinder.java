package org.wildfly.swarm.bootstrap.modules;

import java.io.IOException;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ModuleXmlParserBridge;
import org.jboss.modules.ResourceLoaderSpec;

/**
 * @author Bob McWhirter
 */
public class JBossMSCBootstrapModuleFinder implements ModuleFinder {

    private static final String MSC_VERSION = "1.2.4.Final";

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if (!(identifier.getName().equals("org.jboss.msc") && identifier.getSlot().equals("main"))) {
            return null;
        }

        ModuleSpec.Builder builder = ModuleSpec.build(identifier);

        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("javax.api")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.logging")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.modules")));

        try {
            builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(ModuleXmlParserBridge.createMavenArtifactLoader("org.jboss.msc:jboss-msc:" + MSC_VERSION)));
        } catch (IOException e) {
            throw new ModuleLoadException(e);
        }
        builder.addDependency(DependencySpec.createLocalDependencySpec());

        return builder.create();
    }
}
