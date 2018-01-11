package org.jboss.unimbus.example;

import java.io.IOException;
import java.sql.Driver;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ken Finnigan
 */
@WebServlet
public class MyServlet extends HttpServlet {
    @Inject
    Driver driver;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("Hello from Servlet! " + this.driver);
    }
}
