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
package org.wildfly.swarm.undertow.runtime;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.SameThreadExecutor;
import org.wildfly.swarm.container.runtime.RuntimeServer;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Vetoed;

/**
 * @author Stuart Douglas
 */
@Vetoed
public class GateHandlerWrapper implements HandlerWrapper, RuntimeServer.Opener {

    private final List<Holder> held = new ArrayList<>();

    private volatile boolean open = false;

    @Override
    public synchronized void open() {
        open = true;
        for(Holder holder : held) {
            holder.exchange.dispatch(holder.next);
        }
        held.clear();
    }

    @Override
    public HttpHandler wrap(HttpHandler handler) {
        return new GateHandler(handler);
    }

    @Vetoed
    private final class GateHandler implements HttpHandler {

        private final HttpHandler next;

        private GateHandler(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            if(open) {
                next.handleRequest(exchange);
                return;
            }
            synchronized (GateHandlerWrapper.this) {
                if(open) {
                    next.handleRequest(exchange);
                } else {
                    exchange.dispatch(SameThreadExecutor.INSTANCE, new Runnable() {
                        @Override
                        public void run() {
                            synchronized (GateHandlerWrapper.this) {
                                if(open) {
                                    exchange.dispatch(next);
                                } else {
                                    held.add(new Holder(next, exchange));
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private static final class Holder {
        final HttpHandler next;
        final HttpServerExchange exchange;

        private Holder(HttpHandler next, HttpServerExchange exchange) {
            this.next = next;
            this.exchange = exchange;
        }
    }
}
