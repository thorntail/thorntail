package org.wildfly.swarm.arquillian.adapter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class AnnotationBasedContainerFactory implements ContainerFactory {
    public static final String ANNOTATED_CLASS_NAME = "swarm.arquillian.container.factory.class";

    @Override
    public Container newContainer(String... args) throws Exception {
        String clsName = System.getProperty(ANNOTATED_CLASS_NAME);

        Class<?> cls = Class.forName(clsName);

        Method[] methods = cls.getMethods();

        for (Method method : methods) {
            if (! Modifier.isStatic( method.getModifiers() ) ) {
                continue;
            }

            org.wildfly.swarm.arquillian.adapter.Container anno = method.getAnnotation(org.wildfly.swarm.arquillian.adapter.Container.class);
            if ( anno == null ) {
                continue;
            }

            return (Container) method.invoke( null );
        }

        throw new RuntimeException( "Unable to create a Container from annotated class: " + clsName );
    }
}
