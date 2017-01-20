package org.wildfly.swarm.container.config;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.Swarm;

/**
 * @author Bob McWhirter
 */
public class ClassLoaderConfigLocator extends ConfigLocator {

    public static ClassLoaderConfigLocator system() {
        return new ClassLoaderConfigLocator(ClassLoader.getSystemClassLoader());
    }

    public static ClassLoaderConfigLocator forApplication() throws ModuleLoadException {
        Module appModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(Swarm.APPLICATION_MODULE_NAME));
        return new ClassLoaderConfigLocator(appModule.getClassLoader());
    }

    private ClassLoaderConfigLocator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Stream<URL> locate(String profileName) throws IOException {
        List<URL> located = new ArrayList<>();

        Enumeration<URL> resources = this.classLoader.getResources(PROJECT_PREFIX + profileName + ".yml");

        while (resources.hasMoreElements()) {
            URL each = resources.nextElement();
            located.add(each);
        }

        resources = this.classLoader.getResources(PROJECT_PREFIX + profileName + ".properties");

        while (resources.hasMoreElements()) {
            URL each = resources.nextElement();
            located.add(each);
        }

        return located.stream();
    }

    private final ClassLoader classLoader;

}
