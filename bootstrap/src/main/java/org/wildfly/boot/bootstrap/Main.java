package org.wildfly.boot.bootstrap;

import org.jboss.modules.BootModuleLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Bob McWhirter
 */
public class Main {

    public static final String VERSION;

    static {
        InputStream in = Main.class.getClassLoader().getResourceAsStream("wildfly-boot.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VERSION = props.getProperty( "version", "unknown" );
    }

    public static void main(String[] args) throws Throwable {
        System.setProperty("boot.module.loader", BootModuleLoader.class.getName());

        Module app = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("APP"));
        InputStream in = app.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");

        Manifest manifest = new Manifest(in);
        String mainClassName = (String) manifest.getMainAttributes().get(new Attributes.Name("Main-Class"));

        if ( mainClassName == null ) {
            mainClassName = "org.wildfly.boot.container.DefaultMain";
        }

        Class<?> mainClass = app.getClassLoader().loadClass(mainClassName);
        final Method mainMethod = mainClass.getMethod("main", String[].class);

        final int modifiers = mainMethod.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            throw new NoSuchMethodException("Main method is not static for " + mainClass);
        }

        setupContent();

        Thread.currentThread().setContextClassLoader( app.getClassLoader() );
        mainMethod.invoke(null, new Object[]{args});
    }

    private static void setupContent() throws ModuleLoadException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Module selfContained = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.boot.container"));
        Class<?> contentClass = selfContained.getClassLoader().loadClass( "org.wildfly.boot.container.Content" );
        Method setup = contentClass.getMethod("setup", ClassLoader.class);
        setup.invoke(null, Main.class.getClassLoader());
    }
}
