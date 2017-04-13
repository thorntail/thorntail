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
package org.wildfly.swarm.topology.consul;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.topology.consul.annotationtest.TaggedAdvertiser;
import org.wildfly.swarm.topology.consul.annotationtest.UntaggedAdvertiser;

import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 *         <br>
 *         Date: 4/11/17
 *         Time: 7:43 AM
 */
@Ignore // requires a locally running consul
@RunWith(Arquillian.class)
public class AdvertiseAndTagByAnnotationTest extends AdvertisingTestBase {

    @Deployment(testable = false)
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addClass(TaggedAdvertiser.class);
        deployment.addClass(UntaggedAdvertiser.class);
        deployment.addAllDependencies();

        return deployment;
    }

    @Test
    @RunAsClient
    @SuppressWarnings("unchecked")
    public void shouldAdvertise() throws Exception {
        Map<?, ?> result = getDefinedServicesAsMap();
        List<String> tags = (List<String>) result.get(TaggedAdvertiser.SERVICE_NAME);
        assertThat(tags).containsOnly(TaggedAdvertiser.TAG_NAME, "http");

        tags = (List<String>) result.get(UntaggedAdvertiser.SERVICE_NAME);
        assertThat(tags).containsOnly("http");
    }
}
