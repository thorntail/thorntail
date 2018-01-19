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

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.unimbus.events.EventEmitter;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

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

    public static void run(Class<?> configClass) {
        new UNimbus(configClass).start();
    }

    public UNimbus(Class<?> configClass) {
        this.configClass = configClass;
    }

    public <T> T get(Class<? extends T> cls, Annotation...qualifiers) {
        Set<Bean<?>> beans = this.container.getBeanManager().getBeans(cls, qualifiers);
        Bean<T> bean = (Bean<T>) this.container.getBeanManager().resolve(beans);
        CreationalContext<T> context = this.container.getBeanManager().createCreationalContext(bean);
        return bean.create(context);
    }

    private final Class<?> configClass;

    public UNimbus start() {
        //new Exception().printStackTrace();
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

        Weld weld = new Weld();

        weld.addExtension( new UNimbusProvidingExtension(this) );

        if (configClass != null) {
            weld.addPackages(true, configClass);
        }

        this.container = weld.initialize();

        EventEmitter emitter = this.container.select(EventEmitter.class).get();
        emitter.fireBootstrap();
        emitter.fireScan();
        emitter.fireInitialize();
        emitter.fireDeploy();
        emitter.fireBeforeStart();
        emitter.fireStart();
        emitter.fireAfterStart();

        long endTick = System.currentTimeMillis();
        CoreMessages.MESSAGES.started(format(endTick - startTick));

        return this;
    }

    public void stop() {
        this.container.shutdown();
    }

    private static String format(long ms) {
        long seconds = ms / 1000;
        long milli = ms % 1000;
        return seconds + "." + milli + "s";
    }

    private WeldContainer container;
}
