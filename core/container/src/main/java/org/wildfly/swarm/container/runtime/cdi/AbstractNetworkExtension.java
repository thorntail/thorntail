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
package org.wildfly.swarm.container.runtime.cdi;

import java.util.Set;
import java.util.function.Consumer;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractNetworkExtension<T> implements Extension {

    protected AbstractNetworkExtension(ConfigView configView) {
        this.configView = configView;
    }

    protected abstract void applyConfiguration(T instance);

    @SuppressWarnings("unused")
    void process(@Observes ProcessProducer<?, T> p, BeanManager beanManager) throws Exception {
        p.setProducer(producer(p.getProducer()));
    }

    protected void applyConfiguration(ConfigKey key, Consumer<Object> consumer) {
        Object value = this.configView.valueOf(key);
        if (value != null) {
            consumer.accept(value);
        }
    }

    protected Producer<T> producer(Producer<T> delegate) {
        return new Producer<T>() {
            @Override
            public T produce(CreationalContext<T> ctx) {
                T instance = delegate.produce(ctx);
                applyConfiguration(instance);
                return instance;
            }

            @Override
            public void dispose(T instance) {
                delegate.dispose(instance);
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return delegate.getInjectionPoints();
            }
        };
    }

    protected final ConfigView configView;
}
