package org.wildfly.swarm.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.wildfly.swarm.spi.api.StageConfig;

/**
 * @author Gavin King
 * @author Adam Warski
 * @author Heiko Braun
 */
public class ConfigExtension implements Extension {

    public <X> void processInjectionTarget(@Observes ProcessInjectionTarget<X> pit) {

        final InjectionTarget<X> it = pit.getInjectionTarget();
        final Map<Field, String> configFieldKeys = new HashMap<>();

        AnnotatedType<X> at = pit.getAnnotatedType();
        Annotation annotation = at.getAnnotation(Configured.class);
        if(annotation == null){
            //don't process classes not marked with @Configured annotation
            return;
        }

        for (AnnotatedField<? super X> aField : at.getFields()) {
            if(aField.isAnnotationPresent(ConfigValue.class)) {

                Field field = aField.getJavaMember();
                field.setAccessible(true);
                configFieldKeys.put(field, aField.getAnnotation(ConfigValue.class).value());
            }
        }

        InjectionTarget<X> wrapped = new InjectionTarget<X>() {

            @Override
            public void inject(X instance, CreationalContext<X> ctx) {
                it.inject(instance, ctx);

                try {
                    StageConfig stageConfig = lookup();

                    for (Map.Entry<Field, String> fieldKey: configFieldKeys.entrySet()) {
                        try {
                            String key = fieldKey.getValue();
                            Class<?> type = fieldKey.getKey().getType();

                            fieldKey.getKey().set(
                                    instance, stageConfig.resolve(key).as(type).getValue()
                            );
                        }
                        catch (Exception e) {
                            throw new InjectionException(e);
                        }
                    }
                } catch (NamingException e) {
                    throw new InjectionException(e);
                }
            }

            @Override
            public void postConstruct(X instance) {
                it.postConstruct(instance);
            }

            @Override
            public void preDestroy(X instance) {
                it.dispose(instance);
            }

            @Override
            public void dispose(X instance) {
                it.dispose(instance);
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            @Override
            public X produce(CreationalContext<X> ctx) {
                return it.produce(ctx);
            }

        };

        pit.setInjectionTarget(wrapped);

    }


    static StageConfig lookup() throws NamingException {
        InitialContext context = new InitialContext();
        return (StageConfig) context.lookup("jboss/swarm/stage-config");
    }
}