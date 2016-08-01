package org.wildfly.swarm.bootstrap.modules;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.util.Layout;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmApplicationConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmBootstrapConf;

/**
 * @author Bob McWhirter
 */
public class ContainerModuleFinder extends AbstractSingleModuleFinder {

    public ContainerModuleFinder() {
        super("swarm.container");
    }

    @Override
    public void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException {

        System.err.println("bootstrap START");


        try {
            if (Layout.getInstance().isUberJar()) {
                handleWildFlySwarmApplicationConf(builder);

                builder.addDependency(
                        DependencySpec.createModuleDependencySpec(
                                PathFilters.acceptAll(),
                                PathFilters.acceptAll(),
                                PathFilters.acceptAll(),
                                PathFilters.acceptAll(),
                                ClassFilters.acceptAll(),
                                ClassFilters.acceptAll(),
                                null,
                                ModuleIdentifier.create("org.wildfly.swarm.container", "runtime"), false));

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
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    protected void handleWildFlySwarmApplicationConf(ModuleSpec.Builder builder) throws Exception {
        InputStream appConf = getClass().getClassLoader().getResourceAsStream(WildFlySwarmApplicationConf.CLASSPATH_LOCATION);
        if (appConf != null) {
            WildFlySwarmApplicationConf conf = new WildFlySwarmApplicationConf(appConf);
            conf.getEntries()
                    .stream()
                    .filter( e-> e instanceof WildFlySwarmApplicationConf.FractionModuleEntry )
                    .forEach( e->{
                        ((WildFlySwarmApplicationConf.FractionModuleEntry)e).apply(builder);
                    });
        }
    }

}
