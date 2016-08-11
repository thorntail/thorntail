package org.wildfly.swarm.bootstrap.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.util.Layout;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmApplicationConf;

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


            builder.addDependency(
                    DependencySpec.createModuleDependencySpec(
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            null,
                            ModuleIdentifier.create("org.jboss.jandex" ), false));

            builder.addDependency(
                    DependencySpec.createModuleDependencySpec(
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            null,
                            ModuleIdentifier.create("org.jboss.weld.api", "3" ), false));
            builder.addDependency(
                    DependencySpec.createModuleDependencySpec(
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            null,
                            ModuleIdentifier.create("org.jboss.weld.spi", "3" ), false));
            builder.addDependency(
                    DependencySpec.createModuleDependencySpec(
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            null,
                            ModuleIdentifier.create("org.jboss.weld.core", "3" ), false));
            builder.addDependency(
                    DependencySpec.createModuleDependencySpec(
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            null,
                            ModuleIdentifier.create("org.jboss.weld.se", "3" ), false));

            builder.addDependency(
                    DependencySpec.createModuleDependencySpec(
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            PathFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            ClassFilters.acceptAll(),
                            null,
                            ModuleIdentifier.create("javax.enterprise.api" ), false));

            if (Layout.getInstance().isUberJar()) {
                handleWildFlySwarmApplicationConf(builder);
            } else {
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                Enumeration<URL> results = cl.getResources("wildfly-swarm-bootstrap.conf");

                while (results.hasMoreElements()) {
                    URL each = results.nextElement();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(each.openStream()))) {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                builder.addDependency(
                                        DependencySpec.createModuleDependencySpec(
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                ClassFilters.acceptAll(),
                                                ClassFilters.acceptAll(),
                                                null,
                                                ModuleIdentifier.create(line, "runtime"), false));
                            }
                        }
                    }
                }
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleWildFlySwarmApplicationConf(ModuleSpec.Builder builder) throws Exception {
        InputStream appConf = getClass().getClassLoader().getResourceAsStream(WildFlySwarmApplicationConf.CLASSPATH_LOCATION);
        if (appConf != null) {
            WildFlySwarmApplicationConf conf = new WildFlySwarmApplicationConf(appConf);
            conf.getEntries()
                    .stream()
                    .filter(e -> e instanceof WildFlySwarmApplicationConf.FractionModuleEntry)
                    .forEach(e -> {
                        //((WildFlySwarmApplicationConf.FractionModuleEntry)e).apply(builder);
                        WildFlySwarmApplicationConf.FractionModuleEntry entry = (WildFlySwarmApplicationConf.FractionModuleEntry) e;

                        builder.addDependency(
                                DependencySpec.createModuleDependencySpec(
                                        PathFilters.acceptAll(),
                                        PathFilters.acceptAll(),
                                        PathFilters.acceptAll(),
                                        PathFilters.acceptAll(),
                                        ClassFilters.acceptAll(),
                                        ClassFilters.acceptAll(),
                                        null,
                                        ModuleIdentifier.create(entry.getName(), "runtime"), false));

                        /*
                        builder.addDependency(
                                DependencySpec.createModuleDependencySpec(
                                        PathFilters.acceptAll(),
                                        PathFilters.acceptAll(),
                                        PathFilters.acceptAll(),
                                        PathFilters.acceptAll(),
                                        ClassFilters.acceptAll(),
                                        ClassFilters.acceptAll(),
                                        null,
                                        ModuleIdentifier.create(entry.getName(), "main"), false));
                                        */
                    });
        }
    }

}
