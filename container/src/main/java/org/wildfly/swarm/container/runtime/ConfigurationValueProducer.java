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
package org.wildfly.swarm.container.runtime;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.StageConfig.Resolver;
import org.wildfly.swarm.spi.api.annotations.ConfigurationValue;

/**
 * @author Martin Kouba
 */
@Singleton
public class ConfigurationValueProducer {

    @Inject
    private StageConfig stageConfig;

    @Inject
    private BeanManager beanManager;

    @Produces
    @ConfigurationValue
    @Dependent
    Resolver<String> produceResolver(InjectionPoint injectionPoint) {
        return resolver(injectionPoint);
    }

    @Produces
    @ConfigurationValue
    @Dependent
    String produceStringConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, String.class);
    }


    @Produces
    @ConfigurationValue
    @Dependent
    Integer produceIntegerConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Integer.class);
    }

    @Produces
    @Dependent
    @ConfigurationValue
    Boolean produceBooleanConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Boolean.class);
    }

    @ConfigurationValue
    @Dependent
    @Produces
    Long produceLongConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Long.class);
    }

    @ConfigurationValue
    @Dependent
    @Produces
    Float produceFloatConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Float.class);
    }

    @ConfigurationValue
    @Dependent
    @Produces
    Double produceDoubleConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Double.class);
    }

    private <T> T resolve(InjectionPoint injectionPoint, Class<T> target) {
        Resolver<String> resolver = resolver(injectionPoint);
        try {
            return resolver != null ? resolver.as(target).getValue() : null;
        } catch (RuntimeException e)  {
            return null;
        }
    }

    private Resolver<String> resolver(InjectionPoint injectionPoint) {
        String name = getName(injectionPoint);
        if (name.isEmpty() || stageConfig == null) {
            return null;
        }
        return stageConfig.resolve(getName(injectionPoint));
    }

    private String getName(InjectionPoint injectionPoint) {
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(ConfigurationValue.class)) {
                return ((ConfigurationValue) qualifier).value();
            }
        }
        return null;
    }

    /*
    private StageConfig lookup() {
        try {
            InitialContext context = new InitialContext();
            return (StageConfig) context.lookup("jboss/swarm/stage-config");
        } catch (NamingException e) {
            return null;
        }
    }
    */

}

