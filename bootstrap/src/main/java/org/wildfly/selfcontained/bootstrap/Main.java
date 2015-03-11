package org.wildfly.selfcontained.bootstrap;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.SelfContainedModuleLoader;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Bob McWhirter
 */
public class Main {

    public static void main(String[] args) throws Throwable {
        System.setProperty("boot.module.loader", SelfContainedModuleLoader.class.getName());

        Module app = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("APP"));
        System.err.println("APP MODULE: " + app);
        InputStream in = app.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");

        Manifest manifest = new Manifest(in);
        String mainClassName = (String) manifest.getMainAttributes().get(new Attributes.Name("Main-Class"));

        Class<?> mainClass = app.getClassLoader().loadClass(mainClassName);
        final Method mainMethod = mainClass.getMethod("main", String[].class);

        final int modifiers = mainMethod.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            throw new NoSuchMethodException("Main method is not static for " + mainClass);
        }

        setupContent();

        System.err.println("running main");
        mainMethod.invoke(null, new Object[]{args});
        System.err.println( "completed main" );
    }

    private static void setupContent() throws ModuleLoadException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Module selfContained = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.self-contained"));
        Class<?> contentClass = selfContained.getClassLoader().loadClass( "org.wildfly.selfcontained.Content" );
        Method setup = contentClass.getMethod("setup", ClassLoader.class);
        setup.invoke(null, Main.class.getClassLoader());
    }
}
