package org.jboss.unimbus.jndi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.TypeLiteral;

/**
 * Created by bob on 1/16/18.
 */
public class BinderExtension implements Extension {

    void exposeBinders(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        Set<Bean<?>> binderBeans = beanManager.getBeans(new TypeLiteral<Binder<?>>() {
        }.getType());

        for (Bean<?> each : binderBeans) {
            for (Type type : each.getTypes()) {
                if (type instanceof ParameterizedType) {
                    if (Binder.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType())) {
                        Type boundType = ((ParameterizedType) type).getActualTypeArguments()[0];

                        event.addBean()
                                .addType(boundType)
                                .addType(Object.class)
                                .scope(ApplicationScoped.class)
                                .addQualifier(Any.Literal.INSTANCE)
                                .addQualifier(Default.Literal.INSTANCE)
                                .produceWith((obj) -> {
                                    try {
                                        CreationalContext context = beanManager.createCreationalContext(each);
                                        Binder binder = (Binder) each.create(context);
                                        return binder.get();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                });

                    }
                }
            }
        }
    }
}
