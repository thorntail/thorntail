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
package org.wildfly.swarm.microprofile.faulttolerance.tck;

import java.io.File;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ResourceContainer;

/**
 *
 * @author Martin Kouba
 */
public class FaultToleranceApplicationArchiveProcessor implements ApplicationArchiveProcessor {

    private static final Logger LOGGER = Logger.getLogger(FaultToleranceApplicationArchiveProcessor.class.getName());

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (!(applicationArchive instanceof ResourceContainer)) {
            LOGGER.warning("Unable to add Hystrix-related project-defaults.yaml - not a resource container: " + applicationArchive);
            return;
        }
        ResourceContainer<?> resourceContainer = (ResourceContainer<?>) applicationArchive;
        resourceContainer.addAsResource(new File("src/test/resources/project-defaults.yml"));
        LOGGER.info("Added project-defaults.yaml to " + applicationArchive.toString(true));
    }
}
