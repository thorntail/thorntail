package org.wildfly.swarm.container;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.LogManager;

/**
 * @author Bob McWhirter
 */
public class Main {

    private static final PrintStream STDERR = System.err;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

        Module app = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("APP"));
        InputStream in = app.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");

        Manifest manifest = new Manifest(in);
        String mainClassName = (String) manifest.getMainAttributes().get(new Attributes.Name("Main-Class"));

        if (mainClassName == null) {
            mainClassName = DefaultMain.class.getName();
        }

        Class<?> mainClass = app.getClassLoader().loadClass(mainClassName);
        final Method mainMethod = mainClass.getMethod("main", String[].class);

        final int modifiers = mainMethod.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            throw new NoSuchMethodException("Main method is not static for " + mainClass);
        }

        Thread.currentThread().setContextClassLoader(app.getClassLoader());

        mainMethod.invoke(null, new Object[]{args});
    }
}
