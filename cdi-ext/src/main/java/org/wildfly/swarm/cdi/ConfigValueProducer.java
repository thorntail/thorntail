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

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.StageConfig.Resolver;
import org.wildfly.swarm.spi.api.annotations.ConfigurationValue;

/**
 *
 * @author Martin Kouba
 */
@Singleton
public class ConfigValueProducer {

    /*
    static {
        System.err.println( "Annotation from CVP - " + ConfigurationValue.class.getClassLoader() );
        System.err.println( "Producer from CVP: " + ConfigValueProducer.class.getClassLoader() );
        System.err.println( "NonBinding from CVP: " + Nonbinding.class.getClassLoader() );
    }

    private StageConfig stageConfig;

    public ConfigValueProducer() {
        System.err.println( "******* ConfigValueProducer ctor" );
    }

    @PostConstruct
    void init() {
        System.err.println( "******* ConfigValueProducer POSTCONSTRUCT" );
        stageConfig = lookup();
    }
    */

    @ConfigurationValue
    @Dependent
    @Produces
    public Resolver<String> produceResolver(InjectionPoint injectionPoint) {
        return null;
        //return resolver(injectionPoint);
    }
    /*

    @ConfigurationValue
    @Dependent
    @Produces
    public String produceStringConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, String.class);
    }

    @ConfigurationValue
    @Dependent
    @Produces
    public Integer produceIntegerConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Integer.class);
    }

    @ConfigurationValue
    @Dependent
    @Produces
    public Boolean produceBooleanConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Boolean.class);
    }

    @ConfigurationValue
    @Dependent
    @Produces
    public Long produceLongConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Long.class);
    }

    @ConfigurationValue
    @Dependent
    @Produces
    public Float produceFloatConfigValue(InjectionPoint injectionPoint) {
        return resolve(injectionPoint, Float.class);
    }

    @ConfigurationValue
    @Dependent
    @Produces
    public Double produceDoubleConfigValue(InjectionPoint injectionPoint) {
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
            if (qualifier.annotationType().equals(ConfigurationValue.class)) {
                return ((ConfigurationValue) qualifier).value();
            }
        }
        return null;
    }

    private StageConfig lookup() {
        try {
            InitialContext context = new InitialContext();
            return (StageConfig) context.lookup("jboss/swarm/stage-config");
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        }
    }
    */

}
