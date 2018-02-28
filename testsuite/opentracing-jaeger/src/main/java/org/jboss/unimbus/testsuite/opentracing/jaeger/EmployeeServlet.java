package org.jboss.unimbus.testsuite.opentracing.jaeger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uber.jaeger.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.eclipse.microprofile.opentracing.Traced;

/**
 * @author Ken Finnigan
 */
@WebServlet(name = "EmployeeServlet", urlPatterns = "/*")
@Traced
public class EmployeeServlet extends HttpServlet {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Employee> employees = em.createNamedQuery("Employee.findAll", Employee.class).getResultList();

        PrintWriter writer = resp.getWriter();

        for (Employee employee : employees) {
            employee.getName();
        }

        Tracer tracer = GlobalTracer.get();
        SpanContext ctx = (SpanContext) tracer.activeSpan().context();

        writer.write(Long.toHexString(ctx.getTraceId()));
    }
}