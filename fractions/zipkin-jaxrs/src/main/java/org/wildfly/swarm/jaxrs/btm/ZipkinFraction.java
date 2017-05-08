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

import java.util.UUID;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.LoggingReporter;
import com.github.kristofa.brave.Sampler;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
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
    }

    public ZipkinFraction(String name) {
        setName(name);
    }

    public Brave getBraveInstance() {

        Brave.Builder builder = new Brave.Builder(name.get());

        if (this.url.isDefault()) {
            builder.reporter(new LoggingReporter())
                           .traceSampler(Sampler.create(1.0f));
        } else {
            AsyncReporter<Span> asyncReporter = AsyncReporter.builder(URLConnectionSender.create(url.get())).build();
            builder.reporter(asyncReporter)
                    .traceSampler(Sampler.create(rate.get()));
        }
        return builder.build();
    }

    public ZipkinFraction setName(String name) {
        this.name.set(name);
        return this;
    }

    public ZipkinFraction setUrl(String url) {
        this.url.set(url);
        return this;
    }

    public ZipkinFraction setRate(Float rate) {
        this.rate.set(rate);
        return this;
    }

    @AttributeDocumentation("The service name used in reports")
    private Defaultable<String> name = Defaultable.string(UUID.randomUUID().toString());

    @AttributeDocumentation("URL of the Zipkin server")
    private Defaultable<String> url = Defaultable.string("http://localhost:9411/");

    @AttributeDocumentation("The reporting rate")
    private Defaultable<Float> rate = Defaultable.floating(1.0f);



}
