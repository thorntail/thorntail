/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.unimbus;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.jboss.unimbus.condition.ConditionExtension;
import org.jboss.unimbus.config.ConfigExtension;
import org.jboss.unimbus.spi.UNimbusConfiguration;

/**
 * @author Ken Finnigan
 */
public class UNimbus {
    public static void run() {
        UNimbus.run(null);
    }

    public static void run(Class<? extends UNimbusConfiguration> uNimbusConfig) {
        SeContainerInitializer containerInitializer = SeContainerInitializer.newInstance();
        containerInitializer.addExtensions(new ConditionExtension());
        containerInitializer.addExtensions(new ConfigExtension());
        SeContainer container = containerInitializer.initialize();

        InitializerHandler initializerHandler = container.select(InitializerHandler.class).get();
        initializerHandler.pre();

        if (uNimbusConfig != null) {
            UNimbusConfiguration config = container.select(uNimbusConfig).get();
            config.run();
        }

        initializerHandler.post();
    }
}
