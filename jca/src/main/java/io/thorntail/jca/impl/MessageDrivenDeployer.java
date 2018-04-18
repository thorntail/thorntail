package io.thorntail.jca.impl;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import io.thorntail.Thorntail;
import io.thorntail.jca.ext.MessageDrivenExtension;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.jca.core.spi.rar.Activation;
import org.jboss.jca.core.spi.rar.MessageListener;
import org.jboss.jca.core.spi.rar.NotFoundException;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import io.thorntail.config.impl.ConfigImpl;
import io.thorntail.events.LifecycleEvent;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class MessageDrivenDeployer {

    void deploy(@Observes LifecycleEvent.Deploy event) throws Exception {
        for (Class<?> each : ext.getEntries()) {
            deploy(each);
        }
    }

    void deploy(Class<?> driven) throws Exception {
        String raId = findResourceAdapterId(driven);
        if (raId == null) {
            throw new RuntimeException("no RA");
        }

        List<MessageListener> listeners = this.raRepo.getMessageListeners(raId);

        if (listeners.isEmpty()) {
            throw new RuntimeException("no message listeners");
        }

        MessageListener listener = listeners.iterator().next();
        Activation activation = listener.getActivation();
        ActivationSpec activationSpec = activation.createInstance();

        configure(driven, activationSpec);

        ResourceAdapter ra = this.raRepo.getResourceAdapter(raId);

        Entry entry = new Entry(ra, factory(driven, listener.getType()), activationSpec);

        ra.endpointActivation(entry.factory, entry.spec);
        this.entries.add(entry);
        JCAMessages.MESSAGES.deployedMessageDriven(driven.getName());
    }

    @PreDestroy
    void undeploy() {
        for (Entry entry : this.entries) {
            entry.ra.endpointDeactivation( entry.factory, entry.spec );
        }
    }

    MessageEndpointFactory factory(Class<?> driven, Class<?> listenerInterface) {
        return new EndpointFactory(this.system, driven, listenerInterface);
    }


    void configure(Class<?> driven, ActivationSpec activationSpec) throws Exception {
        Class<? extends ActivationSpec> cls = activationSpec.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(cls);

        MessageDriven anno = driven.getAnnotation(MessageDriven.class);
        ActivationConfigProperty[] properties = anno.activationConfig();

        for (ActivationConfigProperty property : properties) {
            String name = property.propertyName();
            String value = property.propertyValue();

            for (PropertyDescriptor each : beanInfo.getPropertyDescriptors()) {
                if (each.getName().equalsIgnoreCase(name)) {
                    Object coerced = coerce(value, each.getPropertyType());
                    each.getWriteMethod().invoke(activationSpec, coerced);
                }
            }
        }
    }

    private Object coerce(String value, Class<?> propertyType) {
        return ((ConfigImpl) ConfigProviderResolver.instance().getConfig()).convert(value, propertyType).get();
    }

    String findResourceAdapterId(Class<?> driven) throws NotFoundException {
        if (driven == null) {
            return null;
        }
        Class<?>[] interfaces = driven.getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            Set<String> adapterIds = this.raRepo.getResourceAdapters(interfaces[i]);
            if (!adapterIds.isEmpty()) {
                return adapterIds.iterator().next();
            }
        }
        return findResourceAdapterId(driven.getSuperclass());
    }

    @Inject
    ResourceAdapterRepository raRepo;

    @Inject
    MessageDrivenExtension ext;

    @Inject
    Thorntail system;

    private List<Entry> entries = new ArrayList<>();

    static class Entry {
        Entry(ResourceAdapter ra, MessageEndpointFactory factory, ActivationSpec spec) {
            this.ra = ra;
            this.factory = factory;
            this.spec = spec;
        }

        ResourceAdapter ra;

        MessageEndpointFactory factory;

        ActivationSpec spec;
    }
}
