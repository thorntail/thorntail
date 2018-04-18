package io.thorntail.testsuite.servlet.staticcontent;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author Ken Finnigan
 */
@WebServlet(
        urlPatterns = {"/"}
)
public class MyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("Hello from Servlet on port " + port);
    }

    @Inject
    @ConfigProperty(name = "web.primary.port")
    int port;
}
