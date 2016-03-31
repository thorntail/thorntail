package org.wildfly.swarm.ribbon.secured;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wildfly.swarm.netflix.ribbon.secured.client.SecuredRibbon;

@WebServlet("/")
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getOutputStream().println("It worked! " + SecuredRibbon.class.getName());
        resp.getOutputStream().close();
    }
}
