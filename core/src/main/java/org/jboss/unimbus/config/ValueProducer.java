package org.jboss.unimbus.config;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
public class ValueProducer {

    @Produces
    @Value
    int valueOf(InjectionPoint injectionPoint) {
        Object value = null;
        Value valueAnno = injectionPoint.getAnnotated().getAnnotation(Value.class);
        String key = valueAnno.value();
        for (Configuration each : this.configurations) {
            value = each.get(key);
            if (value != null) {
                break;
            }
        }

        if (value != null) {
            return Integer.valueOf(value.toString());
        }

        Set<Bean<?>> beans = beanManager.getBeans(Integer.TYPE, new DefaultValue.Literal(key));
        if ( beans.isEmpty() ) {
            return 0;
        }

        if ( beans.size() > 1 ) {
            throw new RuntimeException( "Too many defaults");
        }

        Bean<Object> bean = (Bean<Object>) beans.iterator().next();
        CreationalContext<Object> context = this.beanManager.createCreationalContext(bean);
        Object defaultValue = bean.create(context);

        if ( defaultValue == null ) {
            return 0;
        }

        System.err.println( "coerce default: " + defaultValue);

        return 0;
    }

    @Inject
    @Any
    Instance<Configuration> configurations;

    @Inject
    BeanManager beanManager;
}
