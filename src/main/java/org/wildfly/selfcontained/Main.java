package org.wildfly.selfcontained;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.SelfContainedModuleLoader;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Set;
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

        System.err.println( "running main" );
        mainMethod.invoke(null, new Object[]{args});
        System.err.println( "completed main" );
    }
}
