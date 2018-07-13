package io.thorntail.health.impl;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;


@SuppressWarnings("serial")
public class HealthServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        SmallRyeHealth health = reporter.getHealth();
        if (health.isDown()) {
            resp.setStatus(503);
        }
        reporter.reportHealth(resp.getOutputStream(), health);
    }

    @Inject
    private SmallRyeHealthReporter reporter;
}


