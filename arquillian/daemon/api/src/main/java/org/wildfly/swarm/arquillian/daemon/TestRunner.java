/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.arquillian.daemon;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.SetupAction;
import org.jboss.modules.ModuleClassLoader;

public class TestRunner {
    private final static String CLASS_NAME_ARQ_TEST_RUNNERS = "org.jboss.arquillian.container.test.spi.util.TestRunners";

    private final static String METHOD_NAME_GET_TEST_RUNNER = "getTestRunner";

    private final static String METHOD_NAME_EXECUTE = "execute";

    private final DeploymentUnit deploymentUnit;

    public TestRunner(DeploymentUnit deploymentUnit) {
        this.deploymentUnit = deploymentUnit;
    }

    public Serializable executeTest(final String testClassName, final String methodName) {

        final ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            // We have to set the TCCL here due to ARQ-1181; if that's resolved we can remove all TCCL mucking
            ModuleClassLoader classLoader = deploymentUnit.getAttachment(Attachments.MODULE).getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);

            final Class<?> testClass;
            try {
                testClass = classLoader.loadClass(testClassName);
            } catch (final ClassNotFoundException cnfe) {
                throw new IllegalStateException("Could not load class " + testClassName);
            }
            final Class<?> testRunnersClass;
            try {
                testRunnersClass = classLoader.loadClass(CLASS_NAME_ARQ_TEST_RUNNERS);
            } catch (final ClassNotFoundException cnfe) {
                throw new IllegalStateException("Could not load class " + CLASS_NAME_ARQ_TEST_RUNNERS);
            }
            final Method getTestRunnerMethod = testRunnersClass.getMethod(METHOD_NAME_GET_TEST_RUNNER, ClassLoader.class);
            final Object testRunner = getTestRunnerMethod.invoke(null, classLoader);
            final Method executeMethod = testRunner.getClass().getMethod(METHOD_NAME_EXECUTE, Class.class, String.class);

            List<SetupAction> setupActions = deploymentUnit.getAttachmentList(Attachments.SETUP_ACTIONS);
            ContextManager contextManager = new ContextManager(setupActions);
            Map<String, Object> props = new HashMap<>();
            try {
                contextManager.setup(props);
                return (Serializable) executeMethod.invoke(testRunner, testClass, methodName);
            } finally {
                contextManager.teardown(props);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException iae) {
            throw new RuntimeException(iae);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }
}
