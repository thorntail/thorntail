package org.wildfly.swarm.opentracing;

import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Pavol Loffay
 */
@WebServlet(urlPatterns = "/_opentracing/async", asyncSupported = true)
public class AsyncServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    AsyncContext asyncContext = req.startAsync();
    asyncContext.start(() -> {
      try {
        asyncContext.getResponse().getWriter().write("async");
      } catch (IOException e) {
        e.printStackTrace();
      }
      asyncContext.complete();
    });
  }
}
