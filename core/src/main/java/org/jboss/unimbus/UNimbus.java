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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.jboss.unimbus.events.EventEmitter;
import org.jboss.unimbus.spi.UNimbusConfiguration;

/**
 * @author Ken Finnigan
 */
public class UNimbus {

    public static final String PROJECT_CODE = "UNIMBUS-";

    public static final String PROJECT_NAME = "uNimbus";

    public static final String PROJECT_KEY = "unimbus";

    public static void run() {
        UNimbus.run(null);
    }

    public static void run(Class<? extends UNimbusConfiguration> uNimbusConfig) {

        long startTick = System.currentTimeMillis();
        /*
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.FINEST);
        }
        */

        Logger noisy = Logger.getLogger("org.jboss.weld.Bootstrap");
        noisy.setLevel(Level.SEVERE);

        noisy = Logger.getLogger("org.jboss.weld.Version");
        noisy.setLevel(Level.SEVERE);

        SeContainerInitializer containerInitializer = SeContainerInitializer.newInstance();
        SeContainer container = containerInitializer.initialize();

        EventEmitter emitter = container.select(EventEmitter.class).get();
        emitter.fireBootstrap();
        emitter.fireScan();
        emitter.fireInitialize();
        emitter.fireDeploy();
        emitter.fireBeforeStart();

        if (uNimbusConfig != null) {
            UNimbusConfiguration config = container.select(uNimbusConfig).get();
            config.run();
        }

        emitter.fireStart();
        emitter.fireAfterStart();

        long endTick = System.currentTimeMillis();

        CoreMessages.MESSAGES.started(format(endTick - startTick));
    }

    private static String format(long ms) {
        long seconds = ms/1000;
        long milli = ms%1000;
        return seconds + "." + milli + "s";
    }
}
