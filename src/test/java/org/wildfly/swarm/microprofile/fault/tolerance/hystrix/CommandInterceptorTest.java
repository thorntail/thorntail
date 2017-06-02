/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import com.netflix.config.ConfigurationManager;
import org.apache.commons.configuration.AbstractConfiguration;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.extension.HystrixExtension;


/**
 * @author Antoine Sabot-Durand
 */
public class CommandInterceptorTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.from(
            new Weld()
                    .addExtension(new HystrixExtension())
                    .addPackages(true
                            , DefaultCommand.class
                            , getClass())
    ).inject(this)
            .build();

    @Test
    public void shouldRunWithLongExecutionTime() {
        Object res = mm.sayHello();
        try {
            Assert.assertEquals("Hello", ((Future) res).get());
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail("Timeout");
        }
    }


    @Test(expected = MicroprofileHystrixException.class)
    public void shouldTimeout() {
        AbstractConfiguration conf = ConfigurationManager.getConfigInstance();

        conf.setProperty("hystrix.command.DefaultCommand.execution.isolation.thread.timeoutInMilliseconds",
                         new Long(1000));
        Object res = mm.sayHello();
        try {
            ((Future) res).get();
            Assert.fail("Should have been in timeout state");
        } catch (InterruptedException | ExecutionException e) {
            throw new MicroprofileHystrixException("expected timeout", e);
        } finally {
            conf.clearProperty("hystrix.command.DefaultCommand.execution.isolation.thread.timeoutInMilliseconds");
        }
    }


    @Test
    public void shouldFallbackAfterTimeout() {
        AbstractConfiguration conf = ConfigurationManager.getConfigInstance();

        conf.setProperty("hystrix.command.DefaultCommand.execution.isolation.thread.timeoutInMilliseconds",
                         new Long(1000));
        Object res = mm.sayHelloWithFailback();
        try {
            Assert.assertEquals("Store is closed", ((Future) res).get());
        } catch (InterruptedException | ExecutionException e) {
            throw new MicroprofileHystrixException("expected timeout", e);
        } finally {
            conf.clearProperty("hystrix.command.DefaultCommand.execution.isolation.thread.timeoutInMilliseconds");

        }
    }


    @Inject
    MyMicroservice mm;

}