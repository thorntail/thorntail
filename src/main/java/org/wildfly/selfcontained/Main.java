package org.wildfly.selfcontained;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.SelfContainedModuleLoader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author Bob McWhirter
 */
public class Main {

    public static void main(String[] args) throws Throwable {
        System.setProperty( "boot.module.loader", SelfContainedModuleLoader.class.getName() );
        Module server = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.jboss.as.server"));
        Class<?> mainClass = server.getClassLoader().loadClassLocal("org.jboss.as.server.SelfContainedMain");

        final Method mainMethod = mainClass.getMethod("main", String[].class);
        final int modifiers = mainMethod.getModifiers();
        if (! Modifier.isStatic(modifiers)) {
            throw new NoSuchMethodException("Main method is not static for " + mainClass);
        }
        // ignore the return value
        mainMethod.invoke(null, new Object[] {args});

        //EmbeddedMain.main(args);
    }
}
