package org.wildfly.swarm.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.util.Layout;

/**
 * @author Bob McWhirter
 */
public class Main {

    public static final String VERSION;

    static {
        InputStream in = Main.class.getClassLoader().getResourceAsStream("wildfly-swarm.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VERSION = props.getProperty("version", "unknown");
    }

    public static void main(String[] args) throws Throwable {
        System.setProperty("boot.module.loader", BootModuleLoader.class.getName());
        Module bootstrap = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap"));

        String mainClassName = null;
        Manifest manifest = Layout.getManifest();

        if (manifest != null) {
            mainClassName = (String) manifest.getMainAttributes().get(new Attributes.Name("Wildfly-Swarm-Main-Class"));
        }

        if (mainClassName == null) {
            mainClassName = "org.wildfly.swarm.Swarm";
        }

        Class<?> mainClass = bootstrap.getClassLoader().loadClass(mainClassName);
        final Method mainMethod = mainClass.getMethod("main", String[].class);

        final int modifiers = mainMethod.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            throw new NoSuchMethodException("Main method is not static for " + mainClass);
        }

        try {
            mainMethod.invoke(null, new Object[]{args});
        } catch (Throwable e) {
            while ( e != null ) {
                e.printStackTrace();
                e = e.getCause();
            }
        }

    }
}
