package io.thorntail.restclient.impl;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author hbraun
 */
public class RestClientExtension implements Extension {

    private static Set<Class<?>> proxyTypes = new LinkedHashSet<>();

    private static Set<Throwable> errors = new LinkedHashSet<>();

    public void registerRestClient(@Observes
                                   @WithAnnotations(RegisterRestClient.class) ProcessAnnotatedType<?> type) {
        Class<?> javaClass = type.getAnnotatedType().getJavaClass();
        if (javaClass.isInterface()) {
            proxyTypes.add(javaClass);
            type.veto();
        } else {
            errors.add(new IllegalArgumentException("Rest client needs to be an interface " + javaClass));
        }
    }

    public void createProxy(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        for (Class<?> proxyType : proxyTypes) {
            afterBeanDiscovery.addBean(new RestClientDelegateBean(proxyType, beanManager));
        }
    }

    public void reportErrors(@Observes AfterDeploymentValidation afterDeploymentValidation) {
        for (Throwable error : errors) {
            afterDeploymentValidation.addDeploymentProblem(error);
        }
    }
}
