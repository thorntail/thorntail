package org.wildfly.embedded.modules;

import modules.system.layers.base.org.jboss.as.server.main.Module;
import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.wildfly.embedded.ArtifactLoaderFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class BaseModule {

    private final String name;
    private final String slot;

    private List<String> artifacts = new ArrayList<>();
    private List<Dependency> dependencies = new ArrayList<>();

    public BaseModule(String name) {
        this(name, "main");
    }

    public BaseModule(String name, String slot) {
        this.name = name;
        this.slot = slot;
    }

    protected void artifact(String gav) {
        this.artifacts.add(gav);
    }

    protected ModuleDependency module(String name) {
        return module(name, "main");
    }

    protected ModuleDependency module(String name, String slot) {
        ModuleDependency dep = new ModuleDependency(name, slot);
        this.dependencies.add(dep);
        return dep;
    }

    protected SystemDependency system() {
        SystemDependency dep = new SystemDependency();
        this.dependencies.add( dep );
        return dep;
    }

    public ModuleSpec getModuleSpec(ArtifactLoaderFactory loaderFactory) throws Exception {

        ModuleSpec.Builder builder = ModuleSpec.build(ModuleIdentifier.create(this.name, this.slot));

        for (String artifact : this.artifacts) {
            ResourceLoader loader = loaderFactory.getLoader(artifact);
            builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(loader));
        }

        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("javax.api")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.staxmapper")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.common-beans"), true, true));

        return builder.create();

    }
}
