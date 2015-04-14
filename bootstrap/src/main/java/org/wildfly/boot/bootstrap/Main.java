package org.wildfly.boot.bootstrap;

import org.jboss.modules.BootModuleLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

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
        Module container = Module.getBootModuleLoader().loadModule( ModuleIdentifier.create( "org.wildfly.boot.container" ) );
        Class<?> mainClass = container.getClassLoader().loadClass("org.wildfly.boot.container.Main");
        final Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, new Object[]{args});
    }
}
