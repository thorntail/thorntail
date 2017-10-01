/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.mp_metrics.cdi;


import javax.enterprise.inject.spi.BeanManager;
import java.util.Collections;

//@Dependent
/* package-private */ class MetricNameFactory {

//    @Produces
    // TODO: should be declared @ApplicationScoped when WELD-2083 is fixed
    private MetricName metricName(BeanManager manager) {
        return new SeMetricName(Collections.emptySet()); // TODO
    }
}
