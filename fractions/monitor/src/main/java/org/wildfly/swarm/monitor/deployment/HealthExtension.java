package org.wildfly.swarm.monitor.deployment;


import org.eclipse.microprofile.health.HealthCheck;
import org.wildfly.swarm.monitor.api.Monitor;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by hbraun on 28.06.17.
 */
public class HealthExtension implements Extension {

    private List<AnnotatedType> delegates = new ArrayList<>();

    public <T> void observeResources(@Observes ProcessAnnotatedType<T> event) {

        AnnotatedType<T> annotatedType = event.getAnnotatedType();
        Class<T> javaClass = annotatedType.getJavaClass();
        for (Class<?> intf : javaClass.getInterfaces()) {
            if (intf.getName().equals(HealthCheck.class.getName())) {
                System.out.println(">> Discovered health check procedure " + javaClass);

                delegates.add(annotatedType);
                /*try {
                    Set<Bean<?>> beans = beanManager.getBeans(annotatedType.getBaseType());
                    Monitor.lookup();

                } catch (Exception e) {
                    throw new RuntimeException("Failed to register health bean", e);
                }*/
            }
        }
    }

    private void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd, BeanManager beanManager) {
        try {
            Monitor monitor = Monitor.lookup();
            for (AnnotatedType delegate : delegates) {
                Set<Bean<?>> beans = beanManager.getBeans(delegate.getBaseType());
                Iterator<Bean<?>> iterator = beans.iterator();
                while (iterator.hasNext()) {
                    Object bean = iterator.next().create(null);// TODO scary hack
                    monitor.registerHealthBean(bean);
                    System.out.println(">> Added health bean impl " + bean);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to register health bean", e);
        }
    }

   /* public <T> void observeInjectionTarget(@Observes ProcessInjectionTarget<T> event)
    {
        if (event.getAnnotatedType() == null)
        { // check for resin's bug http://bugs.caucho.com/view.php?id=3967
            LogMessages.LOGGER.warn(Messages.MESSAGES.annotatedTypeNull());
            return;
        }

        if (Utils.isJaxrsComponent(event.getAnnotatedType().getJavaClass()))
        {
            event.setInjectionTarget(wrapInjectionTarget(event));
        }
    }*/
}
