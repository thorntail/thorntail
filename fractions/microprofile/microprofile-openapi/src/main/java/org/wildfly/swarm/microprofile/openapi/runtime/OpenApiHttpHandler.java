/**
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

package org.wildfly.swarm.microprofile.openapi.runtime;

import java.io.IOException;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.wildfly.swarm.microprofile.openapi.api.OpenApiDocument;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer.Format;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

/**
 * @author Marc Savy {@literal marc@rhymewithgravy.com}
 */
public class OpenApiHttpHandler implements HttpHandler {

    private static final String OAI = "/openapi";
    private static final String ALLOWED_METHODS = "GET, HEAD, OPTIONS";
    private static final String QUERY_PARAM_FORMAT = "format";

    private final Map<Format, String> cachedModels;
    private final HttpHandler next;

    public OpenApiHttpHandler(HttpHandler next) {
        this.next = next;
        this.cachedModels = new ConcurrentHashMap<>();
    }

    /**
     * @see io.undertow.server.HttpHandler#handleRequest(io.undertow.server.HttpServerExchange)
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (OAI.equalsIgnoreCase(exchange.getRequestPath()) && OpenApiDocument.INSTANCE.isSet()) {
            if (exchange.getRequestMethod().equals(Methods.GET)) {
                sendOai(exchange);
            } else if (exchange.getRequestMethod().equals(Methods.OPTIONS))  {
                sendPreflight(exchange);
            } else {
                next.handleRequest(exchange);
            }
        } else {
            next.handleRequest(exchange);
        }
    }

    private void sendPreflight(HttpServerExchange exchange) {
        addCorsResponseHeaders(exchange);
        exchange.getResponseSender().send(ALLOWED_METHODS);
    }


    private void sendOai(HttpServerExchange exchange) throws IOException {
        String accept = exchange.getRequestHeaders().getFirst(Headers.ACCEPT);
        Deque<String> formatQueryParams = exchange.getQueryParameters().get(QUERY_PARAM_FORMAT);
        String formatParam = null;
        if (formatQueryParams != null) {
            formatParam = formatQueryParams.getFirst();
        }

        // Default content type is YAML
        Format format = Format.YAML;

        // Check Accept, then query parameter "format" for JSON; else use YAML.
        if (accept != null && formatParam != null && accept.contains(Format.JSON.getMimeType()) ||
                Format.JSON.getMimeType().equalsIgnoreCase(formatParam)) {
            format = Format.JSON;
        }

        String oai = getCachedOaiString(format);

        addCorsResponseHeaders(exchange);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, format.getMimeType());
        exchange.getResponseSender().send(oai);
    }

    private String getCachedOaiString(Format format) {
        return cachedModels.computeIfAbsent(format, this::getModel);
    }

    private String getModel(Format format) {
        try {
            return OpenApiSerializer.serialize(OpenApiDocument.INSTANCE.get(), format);
        } catch (IOException e) {
            throw new RuntimeException("Unable to serialize OpenAPI in " + format, e);
        }
    }

    private static void addCorsResponseHeaders(HttpServerExchange exchange) {
        HeaderMap headerMap = exchange.getResponseHeaders();
        headerMap.put(new HttpString("Access-Control-Allow-Origin"), "*");
        headerMap.put(new HttpString("Access-Control-Allow-Credentials"), "true");
        headerMap.put(new HttpString("Access-Control-Allow-Methods"), ALLOWED_METHODS);
        headerMap.put(new HttpString("Access-Control-Allow-Headers"), "Content-Type, Authorization");
        headerMap.put(new HttpString("Access-Control-Max-Age"), "86400");
    }

}
