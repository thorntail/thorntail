package org.jboss.unimbus.runner;

import org.jboss.unimbus.UNimbus;

/**
 * Created by bob on 4/3/18.
 */
public class DirectRunner implements Runner {

    public DirectRunner(Class<?> configClass) {
        this.configClass = configClass;
    }

    @Override
    public void run() throws Exception {
        System.err.println( "direct runner");
        new UNimbus(this.configClass).start();
    }

    private final Class<?> configClass;
}
