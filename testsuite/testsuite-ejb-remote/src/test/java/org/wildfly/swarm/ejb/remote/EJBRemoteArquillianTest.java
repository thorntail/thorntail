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
package org.wildfly.swarm.ejb.remote;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import javax.ejb.EJB;

import static org.junit.Assert.assertEquals;

/**
 * This doesn't really test EJB remoting, just local EJB.
 * So the test only verifies that the {@code ejb-remote} fraction can successfully deploy.
 */
@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.JAR)
public class EJBRemoteArquillianTest {
    @EJB(lookup = "java:module/Hello")
    private Hello bean;

    @Test
    public void hello() {
        assertEquals("Hello world", bean.hello());
    }
}
