/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.cdi;

import java.lang.annotation.Annotation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.StageConfig.Resolver;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class ConfigValueProducer {

    private StageConfig stageConfig;

    @PostConstruct
    void init() {
        stageConfig = lookup();
    }

    @ConfigValue
    @Dependent
    @Produces
    Resolver<String> produceResolver(InjectionPoint injectionPoint) {
        return resolver(injectionPoint);
    }

    @ConfigValue
    @Dependent
    @Produces
    String produceStringConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, String.class);
    }

    @ConfigValue
    @Dependent
    @Produces
    Integer produceIntegerConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Integer.class);
    }

    @ConfigValue
    @Dependent
    @Produces
    Boolean produceBooleanConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Boolean.class);
    }

    @ConfigValue
    @Dependent
    @Produces
    Long produceLongConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Long.class);
    }

    @ConfigValue
    @Dependent
    @Produces
    Float produceFloatConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Float.class);
    }

    @ConfigValue
    @Dependent
    @Produces
    Double produceDoubleConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Double.class);
    }

    private <T> T resolve(InjectionPoint injectionPoint, Class<T> target) {
        Resolver<String> resolver = resolver(injectionPoint);
        return resolver != null ? resolver.as(target).getValue() : null;
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
            if (qualifier.annotationType().equals(ConfigValue.class)) {
                return ((ConfigValue) qualifier).value();
            }
        }
        return null;
    }

    private StageConfig lookup() {
        try {
            InitialContext context = new InitialContext();
            return (StageConfig) context.lookup("jboss/swarm/stage-config");
        } catch (NamingException e) {
            return null;
        }
    }

}
