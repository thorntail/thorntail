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
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.topology.TopologyArchive;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 *         <br>
 *         Date: 4/11/17
 *         Time: 7:43 AM
 */
@Ignore // requires a locally running consul
@RunWith(Arquillian.class)
public class AdvertiseAndTagByMainTest extends AdvertisingTestBase {

    private static final Long suffix = new Random().nextLong();
    private static final String serviceName = "service" + suffix;
    private static final String tag1 = "tag1" + suffix;
    private static final String tag2 = "tag2" + suffix;

    @Deployment(testable = false)
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.add(EmptyAsset.INSTANCE, "nothing");
        deployment.as(TopologyArchive.class).advertise(serviceName, asList(tag1, tag2));
        deployment.addAllDependencies();

        return deployment;
    }

    @Test
    @RunAsClient
    @SuppressWarnings("unchecked")
    public void shouldAdvertise() throws Exception {
        Map<?, ?> result = getDefinedServicesAsMap();
        List<String> tags = (List<String>) result.get(serviceName);
        assertThat(tags).containsOnly(tag1, tag2, "http");
    }
}
