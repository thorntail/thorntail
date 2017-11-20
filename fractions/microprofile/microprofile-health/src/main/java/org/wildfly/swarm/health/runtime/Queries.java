/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.health.runtime;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.health.HealthMetaData;
import org.wildfly.swarm.health.api.Monitor;

/**
 * @author Heiko Braun
 * @since 21/03/16
 */
@Vetoed
class Queries {

    protected Queries() {
    }

    /*public final static boolean isHealthEndpoint(Monitor monitor, String relativePath) {
        return query(monitor, metaData -> {
            return relativePath.equals(HttpContexts.HEALTH+metaData.getWebContext());
        });
    }*/

    public static final boolean isSecuredHealthEndpoint(Monitor monitor, String relativePath) {
        return query(monitor, metaData -> {
            return relativePath.equals(HttpContexts.HEALTH + metaData.getWebContext()) && metaData.isSecure();
        });
    }

    public static final boolean isAggregatorEndpoint(Monitor monitor, String relativePath) {
        return query(monitor, metaData -> {
            return relativePath.equals(HttpContexts.HEALTH);
        });
    }

    public static final boolean isDirectAccessToHealthEndpoint(Monitor monitor, String relativePath) {
        return query(monitor, metaData -> {
            return relativePath.equals(metaData.getWebContext());
        });
    }

    public static final boolean query(Monitor monitor, Condition condition) {
        boolean isCondition = false;
        for (HealthMetaData metaData : monitor.getHealthURIs()) {
            isCondition = condition.eval(metaData);
            if (isCondition) {
                break;
            }
        }

        return isCondition;
    }

    @FunctionalInterface
    public interface Condition {
        boolean eval(HealthMetaData metaData);
    }


}
