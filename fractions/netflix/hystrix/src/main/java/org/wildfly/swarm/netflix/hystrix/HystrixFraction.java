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
package org.wildfly.swarm.netflix.hystrix;

import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;

import static org.wildfly.swarm.netflix.hystrix.HystrixProperties.DEFAULT_STREAM_PATH;
import static org.wildfly.swarm.spi.api.Defaultable.string;

/**
 * @author Ken Finnigan
 */
@DeploymentModules({
        @DeploymentModule(name = "com.netflix.hystrix"),
        @DeploymentModule(name = "io.reactivex.rxjava")
})
public class HystrixFraction implements Fraction<HystrixFraction> {

    public HystrixFraction streamPath(String streamPath) {
        this.streamPath.set( streamPath );
        return this;
    }

    public String streamPath() {
        return this.streamPath.get();
    }

    @Configurable("swarm.hystrix.stream.path")
    private Defaultable<String> streamPath = string(DEFAULT_STREAM_PATH);
}
