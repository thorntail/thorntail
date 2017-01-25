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
package org.wildfly.swarm.hystrix.test;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.JAR)
public class ArqHystrixTest {

    @Test
    public void testConfigurationThroughArchaius() {
        HystrixPropertiesStrategy strategy = HystrixPlugins.getInstance().getPropertiesStrategy();

        HystrixCommandProperties defaultProps = strategy.getCommandProperties(HystrixCommandKey.Factory.asKey("default"), HystrixCommandProperties.Setter());

        assertFalse( defaultProps.circuitBreakerEnabled().get());
        assertEquals(77, (int) defaultProps.circuitBreakerErrorThresholdPercentage().get());

        HystrixCommandProperties gooberProps = strategy.getCommandProperties(HystrixCommandKey.Factory.asKey("goober"), HystrixCommandProperties.Setter());
        assertTrue( gooberProps.circuitBreakerEnabled().get());
        assertEquals(44, (int) gooberProps.circuitBreakerErrorThresholdPercentage().get());
    }

}
