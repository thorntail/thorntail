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
package org.wildfly.swarm.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * Adapted from javax.enterprise.inject.spi.Unmanaged by supporting passing an instance to be injected into.
 * This is necessary so that the Class of the instance can be loaded from a different ClassLoader than
 * Weld is executing in.
 *
 * @author Pete Muir
 * @author Ken Finnigan
 */
public class UnmanagedInstance {

    private CreationalContext ctx;
    private InjectionTarget injectionTarget;
    private Object instance;
    private boolean disposed = false;
    private boolean inWeld = true;

    public UnmanagedInstance(Object instance) {
        // Handle case where CDI hasn't been initialized, typically `new Container()` in a custom main()
        try {
            this.instance = instance;

            BeanManager beanManager = CDI.current().getBeanManager();
            AnnotatedType type = beanManager.createAnnotatedType(instance.getClass());
            this.injectionTarget = beanManager.getInjectionTargetFactory(type).createInjectionTarget(null);
            this.ctx = beanManager.createCreationalContext(null);
        } catch (IllegalStateException ise) {
            // Do Nothing, we're not executing in Weld
            this.inWeld = false;
        }
    }

    public Object get() {
        return instance;
    }

    public UnmanagedInstance produce() {
        if (!inWeld) {
            return this;
        }

        if (this.instance != null) {
            throw new IllegalStateException("Trying to call produce() on already constructed instance");
        }
        if (disposed) {
            throw new IllegalStateException("Trying to call produce() on an already disposed instance");
        }
        this.instance = injectionTarget.produce(ctx);
        return this;
    }

    public UnmanagedInstance inject() {
        if (!inWeld) {
            return this;
        }

        if (this.instance == null) {
            throw new IllegalStateException("Trying to call inject() before produce() was called");
        }
        if (disposed) {
            throw new IllegalStateException("Trying to call inject() on already disposed instance");
        }
        injectionTarget.inject(instance, ctx);
        return this;
    }

    public UnmanagedInstance postConstruct() {
        if (!inWeld) {
            return this;
        }

        if (this.instance == null) {
            throw new IllegalStateException("Trying to call postConstruct() before produce() was called");
        }
        if (disposed) {
            throw new IllegalStateException("Trying to call postConstruct() on already disposed instance");
        }
        injectionTarget.postConstruct(instance);
        return this;
    }

    public UnmanagedInstance preDestroy() {
        if (!inWeld) {
            return this;
        }

        if (this.instance == null) {
            throw new IllegalStateException("Trying to call preDestroy() before produce() was called");
        }
        if (disposed) {
            throw new IllegalStateException("Trying to call preDestroy() on already disposed instance");
        }
        injectionTarget.preDestroy(instance);
        return this;
    }

    public UnmanagedInstance dispose() {
        if (!inWeld) {
            return this;
        }

        if (this.instance == null) {
            throw new IllegalStateException("Trying to call dispose() before produce() was called");
        }
        if (disposed) {
            throw new IllegalStateException("Trying to call dispose() on already disposed instance");
        }
        injectionTarget.dispose(instance);
        ctx.release();
        return this;
    }

}
