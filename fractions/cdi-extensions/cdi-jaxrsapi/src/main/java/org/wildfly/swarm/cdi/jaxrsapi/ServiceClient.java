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
package org.wildfly.swarm.cdi.jaxrsapi;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;

/**
 * @author Ken Finnigan
 */
public interface ServiceClient<T> {

    default <U> void exec(Supplier<U> restMethod, Consumer<U> handler, Consumer<Throwable> exceptionHandler) throws Exception {
        chainableExec(restMethod, exceptionHandler)
                .thenAccept(handler)
                .exceptionally(t -> {
                    exceptionHandler.accept(t);
                    return null;
                });
    }

    default <U> CompletableFuture<U> chainableExec(Supplier<U> restMethod, Consumer<Throwable> exceptionHandler) throws Exception {
        return CompletableFuture
                .supplyAsync(restMethod, executorService())
                .exceptionally(t -> {
                    exceptionHandler.accept(t);
                    return null;
                });
    }

    default ManagedExecutorService executorService() throws Exception {
        InitialContext ctx = new InitialContext();
        return (ManagedExecutorService) ctx.lookup("java:jboss/ee/concurrency/executor/default");
    }
}
