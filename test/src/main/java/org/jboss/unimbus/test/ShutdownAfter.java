package org.jboss.unimbus.test;

import org.jboss.unimbus.UNimbus;
import org.junit.runners.model.Statement;

/**
 * Created by bob on 1/19/18.
 */
public class ShutdownAfter extends Statement {

    public ShutdownAfter(UNimbus system, Statement delegate) {
        this.delegate = delegate;
        this.system = system;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            this.delegate.evaluate();
        } finally {
            this.system.stop();
        }
    }

    private final UNimbus system;
    private final Statement delegate;
}
