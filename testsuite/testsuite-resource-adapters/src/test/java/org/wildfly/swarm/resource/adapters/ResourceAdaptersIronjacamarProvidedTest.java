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
package org.wildfly.swarm.resource.adapters;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ralf Battenfeld
 */
@RunWith(Arquillian.class)
public class ResourceAdaptersIronjacamarProvidedTest {

    @Deployment(testable = false)
    public static Archive<?> createDeployment1() {
        final File[] files = Maven.resolver().resolve("net.java.xadisk:xadisk:jar:1.2.2").withoutTransitivity().asFile();
        final RARArchive deploymentRar = ShrinkWrap.create(RARArchive.class, "xadisk.rar");
        deploymentRar.addAsLibraries(files[0]);
        deploymentRar.addAsManifestResource("ironjacamar.xml", "ironjacamar.xml");
        deploymentRar.setResourceAdapterXML("ra.xml");
        return deploymentRar;
    }

    @Test
    @RunAsClient
    public void testNothing() {
    }

}
