package io.thorntail.jca.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.MessageDriven;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.resource.cci.MessageListener;

/**
 * Created by bob on 2/8/18.
 */
public class MessageDrivenExtension implements Extension {

    <T> void process(@Observes @WithAnnotations({MessageDriven.class}) ProcessAnnotatedType<T> event) {
        this.entries.add(event.getAnnotatedType().getJavaClass());
    }

    void abf(@Observes AfterBeanDiscovery event, BeanManager manager) {
        Set<Bean<?>> listenerBeans = manager.getBeans(MessageListener.class);

        for (Bean<?> listenerBean : listenerBeans) {
            System.err.println( "BEAN: " + listenerBean);

        }


    }

    public List<Class<?>> getEntries() {
        return this.entries;
    }

    private List<Class<?>> entries = new ArrayList<>();
}
