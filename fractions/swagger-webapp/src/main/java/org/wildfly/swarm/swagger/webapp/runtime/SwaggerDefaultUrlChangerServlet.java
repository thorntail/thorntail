package org.wildfly.swarm.swagger.webapp.runtime;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

/**
 * this servlet will listen to /swagger-ui and if swarm.swagger.web-app.json.path
 * is configured redirect to index.html?url= so swagger will load the swagger.json
 * configured.
 * @author john
 *
 */
@ApplicationScoped
@WebServlet("")
public class SwaggerDefaultUrlChangerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    @Inject
    @ConfigurationValue ("swarm.swagger.web-app.json.path")
    private String path;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      if (path == null) {
          response.sendRedirect("index.html");
      } else {
          response.sendRedirect("index.html?url=" + path);
      }
    }

}
