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
package org.wildfly.swarm.integration.jsf;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class JSFTest extends AbstractWildFlySwarmTestCase {
    private Container container;

    //@Test
    //TODO Once https://issues.jboss.org/browse/WFLY-4889 is resolved, attempt to get the test working
    public void testSimple() throws Exception {
        container = newContainer();
        container.start();

        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.staticContent();
        container.deploy(deployment);

        String result = fetch(DEFAULT_URL);
        assertThat(result).contains("This is static.");
    }

    @After
    public void shutdown() throws Exception {
        container.stop();
    }
}
