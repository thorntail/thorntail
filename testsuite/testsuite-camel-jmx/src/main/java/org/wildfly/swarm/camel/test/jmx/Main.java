/*
 * #%L
 * Camel JMX :: Tests
 * %%
 * Copyright (C) 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.wildfly.swarm.camel.test.jmx;

import org.apache.camel.builder.RouteBuilder;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.camel.core.CamelCoreFraction;

/**
 * Deploys a test which monitors an JMX attribute of a route.
 *
 * @author thomas.diesler@jboss.com
 * @since 03-Jun-2013
 */
public class Main {

    public static void main(String... args) throws Exception {
        Swarm swarm = new Swarm(args).fraction(new CamelCoreFraction().addRouteBuilder(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .transform(simple("Hello ${body}"));
            }
        }));

        swarm.start().deploy();
    }
}
