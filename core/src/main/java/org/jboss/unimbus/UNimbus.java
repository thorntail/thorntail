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

import org.jboss.unimbus.spi.UNimbusConfiguration;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * @author Ken Finnigan
 */
public class UNimbus {
    public static void run() {
        UNimbus.run(DefaultUNimbusConfiguration.class);
    }

    public static void run(Class<? extends UNimbusConfiguration> uNimbusConfig) {
        Weld weld = new Weld();
        weld.property("org.jboss.weld.se.shutdownHook", false);
        WeldContainer weldContainer = weld.initialize();
        UNimbusConfiguration config = weldContainer.select(uNimbusConfig).get();
        config.run();
    }
}
