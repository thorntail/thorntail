package io.thorntail.metrics.impl.endpoint;

import io.smallrye.metrics.MetricsRequestHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Created by bob on 1/22/18.
 */
public class MetricsServlet extends HttpServlet {

    private final MetricsRequestHandler metricsHandler = new MetricsRequestHandler();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        Stream<String> acceptHeaders = Collections.list(request.getHeaders("Accept")).stream();

        metricsHandler.handleRequest(requestPath, method, acceptHeaders, (status, message, headers) -> {
            headers.forEach(response::addHeader);
            response.setStatus(status);
            response.getWriter().write(message);
        });
    }
}
