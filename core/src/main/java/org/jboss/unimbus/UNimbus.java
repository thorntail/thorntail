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
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.unimbus.events.EventEmitter;
import org.jboss.unimbus.logging.jdk.DefaultConsoleFormatter;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * @author Ken Finnigan
 */
public class UNimbus {

    public static final String PROJECT_CODE = "UNIMBUS-";

    public static final String PROJECT_NAME = "uNimbus";

    public static final String PROJECT_KEY = "unimbus";

    public static final String BASE_LOGGER_CATEGORY = "org.jboss.unimbus";

    public static String loggerCategory(String name) {
        return BASE_LOGGER_CATEGORY + "." + name;
    }

    public static void run() {
        UNimbus.run(null);
    }

    public static void run(Class<?> configClass) {
        new UNimbus(configClass).start();
    }

    public static void main(String...args) {
        run();
    }

    public UNimbus() {
    }

    public UNimbus(Class<?> configClass) {
        this.configClass = configClass;
    }

    public UNimbus(ClassLoader classLoader) {
        this.classLoader = classLoader;

    }

    public <T> T get(Class<? extends T> cls, Annotation... qualifiers) {
        Set<Bean<?>> beans = this.container.getBeanManager().getBeans(cls, qualifiers);
        if (beans.isEmpty()) {
            throw new UnsatisfiedResolutionException();
        }
        Bean<T> bean = (Bean<T>) this.container.getBeanManager().resolve(beans);
        CreationalContext<T> context = this.container.getBeanManager().createCreationalContext(bean);
        return bean.create(context);
    }


    public UNimbus start() {

        bootstrapLogging();

        CoreMessages.MESSAGES.starting();
        long startTick = System.currentTimeMillis();
        long curTick = startTick;

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

        weld.addExtension(new UNimbusProvidingExtension(this));

        if (this.configClass != null) {
            weld.addPackages(true, this.configClass);
        }

        if (this.classLoader != null) {
            weld.setClassLoader(this.classLoader);
            Thread.currentThread().setContextClassLoader(this.classLoader);
        }

        this.container = weld.initialize();
        curTick = markTiming("CDI initialize", curTick);

        EventEmitter emitter = this.container.select(EventEmitter.class).get();
        emitter.fireBootstrap();

        curTick = markTiming("bootstrap", curTick);
        emitter.fireScan();
        curTick = markTiming("scan", curTick);
        emitter.fireInitialize();
        curTick = markTiming("initialize", curTick);
        emitter.fireDeploy();
        curTick = markTiming("deploy", curTick);
        emitter.fireBeforeStart();
        curTick = markTiming("before start", curTick);
        emitter.fireStart();
        curTick = markTiming("start", curTick);
        emitter.fireAfterStart();
        curTick = markTiming("after start", curTick);

        long endTick = System.currentTimeMillis();
        CoreMessages.MESSAGES.started(format(endTick - startTick));

        return this;
    }


    private long markTiming(String phase, long startTick) {
        long curTick = System.currentTimeMillis();
        CoreMessages.MESSAGES.timing(phase, format(curTick - startTick));
        return curTick;
    }

    public void stop() {
        CoreMessages.MESSAGES.stopping();
        ConfigProviderResolver.instance().releaseConfig(ConfigProvider.getConfig());
        this.container.shutdown();
        CoreMessages.MESSAGES.stopped();
    }

    private static String format(long ms) {
        long seconds = ms / 1000;
        long milli = ms % 1000;
        return seconds + "." + milli + "s";
    }

    public BeanManager getBeanManager() {
        return this.container.getBeanManager();
    }

    private void bootstrapLogging() {
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.ALL);
        for (Handler handler : logger.getHandlers()) {
            handler.setLevel(Level.ALL);
            if (handler instanceof ConsoleHandler) {
                handler.setFormatter(new DefaultConsoleFormatter("%4$-6s: %1$tc [%3$s] %5$s%6$s%n"));
            }
        }
        logger.setLevel(Level.INFO);
    }


    private ClassLoader classLoader = UNimbus.class.getClassLoader();

    private Class<?> configClass;

    private WeldContainer container;
}
