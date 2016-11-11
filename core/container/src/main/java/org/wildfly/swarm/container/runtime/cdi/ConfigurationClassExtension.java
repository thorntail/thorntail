/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.container.runtime.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.Configurable;
import org.wildfly.swarm.spi.api.annotations.ConfigurationName;

/**
 * @author Ken Finnigan
 */
public class ConfigurationClassExtension implements Extension {
    /*

    private List<Class<?>> configurationClasses = new ArrayList<>();

    private List<Configurable<?>> configurables = new ArrayList<>();

    public List<Configurable<?>> configurationItems() {
        return this.configurables;
    }

    void processAnnotatedType(@Observes ProcessAnnotatedType pat) {
        if (isConfigurationClass(pat.getAnnotatedType().getJavaClass())) {
            pat.configureAnnotatedType().add(DefaultLiteral.INSTANCE);
            pat.configureAnnotatedType().add(new AnnotationLiteral<Singleton>() { });
        }
    }

    void processInjectionTarget(@Observes ProcessInjectionTarget pit, BeanManager beanManager) throws InstantiationException, IllegalAccessException {
        if (isConfigurationClass(pit.getAnnotatedType().getJavaClass())) {
            ConfigurationClassInjectionTarget wrapped = new ConfigurationClassInjectionTarget(
                    pit.getInjectionTarget(),
                    pit.getAnnotatedType().getJavaClass(),
                    beanManager);

            this.configurables.addAll( wrapped.configurationItems() );
            pit.setInjectionTarget(wrapped);
        }
    }

    protected boolean isConfigurationClass(Class<?> cls) {

        Class<?> cur = cls;

        while (cur != null) {
            if (cur.isAnnotationPresent(ConfigurationName.class)) {
                return true;
            }
            cur = cur.getEnclosingClass();
        }

        return false;
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {
        System.err.println( "exposing ConfigurationItems" );

        for (Configurable<?> each : this.configurables) {
            System.err.println( "expose: " + each );
            abd.addBean()
                    .types( Configurable.class )
                    .scope( Dependent.class )
                    .qualifiers( DefaultLiteral.INSTANCE )
                    .producing(each);
        }
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager beanManager) {
        System.err.println( "initializing ConfigurationItems" );

        StageConfig stageConfig = stageConfig(beanManager);

        Set<Bean<?>> beans = beanManager.getBeans(Configurable.class, AnyLiteral.INSTANCE);

        for (Bean<?> bean : beans) {
            System.err.println( "item: " + bean );
            CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
            Configurable<?> item = (Configurable<?>) beanManager.getReference( bean, Configurable.class, ctx );
            instill( stageConfig, item );
            item.apply();
        }
    }

    private StageConfig stageConfig(BeanManager beanManager) {
        Set<Bean<?>> beans = beanManager.getBeans(StageConfig.class, AnyLiteral.INSTANCE);
        Bean<?> bean = beans.iterator().next();
        CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
        return (StageConfig) beanManager.getReference(bean, StageConfig.class, ctx);
    }

    private <T> void instill(StageConfig stageConfig, Configurable<T> item) {
        System.err.println( "instill: " + item.fullyQualifiedName() );
        StageConfig.Resolver<?> resolver = stageConfig.resolve(item.fullyQualifiedName());
        resolver = resolver.as(item.type());

        if (resolver.hasValue()) {
            Object resolvedValue = resolver.getValue();
            System.err.println( "resolved to: " + resolvedValue );
            item.set(item.type().cast(resolvedValue));
        } else {
            System.err.println( "unresolved" );
        }
    }
    */

    /**
     * Once all beans have been discovered by Weld, for each custom fraction that we have,
     * add the Bean instance to Weld as a replacement for the @DefaultFraction instance we vetoed.
     */
    /*
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {
        super.afterBeanDiscovery(abd, beanManager);

        findConfigurationClasses()
                .forEach(e -> {
                    abd.addBean()
                            .types(e.type())
                            .qualifiers(DefaultLiteral.INSTANCE)
                            .scope(Singleton.class)
                            .produceWith(e);

                    this.configurationItems.addAll(e.configurationItems());
                });
    }
    */
}
