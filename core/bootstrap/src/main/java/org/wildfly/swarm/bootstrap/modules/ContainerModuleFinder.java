package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.performance.Performance;

/**
 * @author Bob McWhirter
 */
public class ContainerModuleFinder extends AbstractSingleModuleFinder {

    private static final String RUNTIME_SLOT = "runtime";

    public ContainerModuleFinder() {
        super("swarm.container");
    }

    @Override
    public void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException {
        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        ModuleIdentifier.create("org.wildfly.swarm.spi"), false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        ModuleIdentifier.create("org.wildfly.swarm.container", RUNTIME_SLOT), false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        ModuleIdentifier.create("org.wildfly.swarm.bootstrap"), false));


        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        ModuleIdentifier.create("org.jboss.jandex"), false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        ModuleIdentifier.create("org.jboss.weld.se"), false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        ModuleIdentifier.create("javax.enterprise.api"), false));

        ApplicationEnvironment environment = ApplicationEnvironment.get();

        environment.bootstrapModules()
                .forEach((module) -> {
                    builder.addDependency(
                            DependencySpec.createModuleDependencySpec(
                                    PathFilters.acceptAll(),
                                    PathFilters.acceptAll(),
                                    PathFilters.acceptAll(),
                                    PathFilters.acceptAll(),
                                    ClassFilters.acceptAll(),
                                    ClassFilters.acceptAll(),
                                    null,
                                    ModuleIdentifier.create(module, RUNTIME_SLOT), false));
                });
    }

}
