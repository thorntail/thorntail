package org.jboss.unimbus.jca.ext;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.MessageDriven;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

/**
 * Created by bob on 2/8/18.
 */
public class MessageDrivenExtension implements Extension {

    <T> void process(@Observes @WithAnnotations({MessageDriven.class}) ProcessAnnotatedType<T> event) {
        this.entries.add(event.getAnnotatedType().getJavaClass());
    }

    public List<Class<?>> getEntries() {
        return this.entries;
    }

    private List<Class<?>> entries = new ArrayList<>();
}
