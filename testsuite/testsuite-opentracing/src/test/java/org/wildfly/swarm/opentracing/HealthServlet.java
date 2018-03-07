package org.wildfly.swarm.opentracing;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.opentracing.contrib.web.servlet.filter.TracingFilter.SERVER_SPAN_CONTEXT;

/**
 * @author Juraci Paixão Kröhling
 */
@WebServlet(urlPatterns = "/_opentracing/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Tracer tracer = GlobalTracer.get();
        SpanContext context = (SpanContext) req.getAttribute(SERVER_SPAN_CONTEXT);
        tracer.buildSpan("health-check-1").asChildOf(context).startActive(true).close();
        resp.getWriter().write("alive");
    }
}
