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
package org.wildfly.swarm.cdi.test.basic;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;
import org.wildfly.swarm.cdi.test.basic.Cheddar;

import static org.junit.Assert.assertNotNull;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class CDIArquillianTest {

    @Inject
    private Cheddar cheddar;

    @Test
    public void testInjection() {
        assertNotNull(cheddar);
    }

    @Test
    public void testCDIContainerPresence() throws Exception {
        assertNotNull(CDI.current());
    }
}
