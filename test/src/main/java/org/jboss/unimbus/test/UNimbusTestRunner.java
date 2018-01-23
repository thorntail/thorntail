package org.jboss.unimbus.test;

import org.eclipse.microprofile.config.ConfigProvider;
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
        configureEphemeralPorts();
        try {
            this.system = new UNimbus(getTestClass().getJavaClass()).start();
            super.run(notifier);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            EphemeralPortsConfigSource.INSTANCE.reset();
            if ( this.system != null ) {
                this.system.stop();
            }
        }
    }

    private void configureEphemeralPorts() {
        EphemeralPortsConfigSource.INSTANCE.reset();
        EphemeralPorts anno = getTestClass().getJavaClass().getAnnotation(EphemeralPorts.class);
        if ( anno != null ) {
            EphemeralPortsConfigSource.INSTANCE.setPrimaryIsEphemeral( anno.primary() );
            EphemeralPortsConfigSource.INSTANCE.setManagementIsEphemeral( anno.management() );
        }
    }

    private UNimbus system;
}
