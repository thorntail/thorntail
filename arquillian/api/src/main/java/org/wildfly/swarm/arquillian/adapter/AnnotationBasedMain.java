package org.wildfly.swarm.arquillian.adapter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.container.Container;

/**
 * @uathor Ken Finnigan
 */
public class AnnotationBasedMain {
    public static final String ANNOTATED_CLASS_NAME = "swarm.arquillian.createswarm.class";

    public static void main(String... args) throws Exception {
        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", "org.wildfly.swarm.bootstrap.modules.BootModuleLoader");
        }

        String clsName = System.getProperty(ANNOTATED_CLASS_NAME);

        Class<?> cls = Class.forName(clsName);

        Method[] methods = cls.getMethods();

        for (Method method : methods) {
            if (! Modifier.isStatic(method.getModifiers() ) ) {
                continue;
            }

            CreateSwarm anno = method.getAnnotation(CreateSwarm.class);
            if ( anno == null ) {
                continue;
            }

            ((Swarm)method.invoke(null)).start().deploy();
        }

    }
}
