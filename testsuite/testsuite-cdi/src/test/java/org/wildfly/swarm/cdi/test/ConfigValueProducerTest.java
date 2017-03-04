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
package org.wildfly.swarm.cdi.test;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
@Ignore
@DefaultDeployment(type = DefaultDeployment.Type.JAR)
public class ConfigValueProducerTest {

    @Test
    public void testInjection(ConfigAwareBean configAwareBean) {
        assertNotNull(configAwareBean);

        assertEquals(Integer.valueOf(10), configAwareBean.getPortOffset());
        assertEquals("DEBUG", configAwareBean.getLogLevel());
        assertEquals(Integer.valueOf(10), configAwareBean
                .getPortOffsetResolver().as(Integer.class).getValue());
    }

}
