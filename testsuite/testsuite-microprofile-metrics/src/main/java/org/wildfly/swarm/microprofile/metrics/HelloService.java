/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.metrics;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

@ApplicationScoped
public class HelloService {

    @Counted(name = "hello-count", absolute = true, displayName = "Hello Count", description = "Number of hello invocations")
    public String hello() {
        return "Hello from counted method";
    }

    @Timed(unit = MetricUnits.MILLISECONDS, name = "howdy-time", absolute = true, displayName = "Howdy Time", description = "Time of howdy invocations")
    public String howdy() {
        return "Howdy from timed method";
    }
}
