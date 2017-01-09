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
package org.wildfly.swarm.jaxrs.btm;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.LoggingReporter;
import com.github.kristofa.brave.Sampler;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

/**
 * @author Heiko Braun
 */
@DeploymentModules({
        @DeploymentModule(name = "com.fasterxml.jackson.jaxrs.jackson-jaxrs-json-provider"),
        @DeploymentModule(name = "io.zipkin.brave"),
        @DeploymentModule(name = "org.wildfly.swarm.jaxrs.btm")
})
public class ZipkinFraction implements Fraction<ZipkinFraction> {

    public ZipkinFraction() {
        this.builder = new Brave.Builder();
    }

    public ZipkinFraction(String serviceName) {
        this.builder = new Brave.Builder(serviceName);
    }

    @Override
    public ZipkinFraction applyDefaults() {
        builder.reporter(new LoggingReporter())
                .traceSampler(Sampler.create(1.0f));
        return this;
    }

    public ZipkinFraction reportAsync(String url) {
        AsyncReporter<Span> asyncReporter = AsyncReporter.builder(URLConnectionSender.create(url)).build();

        builder.reporter(asyncReporter)
                .traceSampler(Sampler.create(1.0f));
        return this;
    }

    public ZipkinFraction sampleRate(float rate) {
        builder.traceSampler(Sampler.create(rate));
        return this;
    }


    public Brave getBraveInstance() {
        return builder.build();
    }

    private final Brave.Builder builder;

}
