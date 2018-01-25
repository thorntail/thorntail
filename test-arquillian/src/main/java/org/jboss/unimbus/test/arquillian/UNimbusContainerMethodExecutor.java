package org.jboss.unimbus.test.arquillian;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.unimbus.UNimbus;

/**
 * Created by bob on 1/25/18.
 */

public class UNimbusContainerMethodExecutor implements ContainerMethodExecutor {

    public UNimbusContainerMethodExecutor(UNimbus system) {
        this.system = system;
    }

    @Override
    public TestResult invoke(TestMethodExecutor executor) {
        Object instance = executor.getInstance();
        Method method = executor.getMethod();
        Object injectedInstance = injectedInstance = this.system.get(instance.getClass());
        try {
            method.invoke(injectedInstance);
        } catch (IllegalAccessException e) {
            return TestResult.failed(e);
        } catch (InvocationTargetException e) {
            return TestResult.failed(e.getCause());
        } catch (Throwable t) {
            return TestResult.failed(t);
        }
        return TestResult.passed();
    }

    private final UNimbus system;
}
