package org.jboss.unimbus.test;

import org.jboss.unimbus.UNimbus;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Created by bob on 1/19/18.
 */
public class UNimbusTestRunner extends BlockJUnit4ClassRunner  {

    public UNimbusTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Object createTest() throws Exception {
        return this.system.get(getTestClass().getJavaClass());
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            this.system = new UNimbus(getTestClass().getJavaClass()).start();
            super.run(notifier);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            if ( this.system != null ) {
                this.system.stop();
            }
        }
    }

    private UNimbus system;
}
