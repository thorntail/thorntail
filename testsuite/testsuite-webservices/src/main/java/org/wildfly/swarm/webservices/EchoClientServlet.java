package org.wildfly.swarm.webservices;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceRef;

/**
 * @author Ken Finnigan
 */
@WebServlet("/client")
public class EchoClientServlet extends HttpServlet {

    @WebServiceRef(EchoServiceClient.class)
    private EchoService echoService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String input = req.getParameter("message");
        final String echo = echoService.echo(input);

        resp.setContentType("text/plain");
        final PrintWriter writer = resp.getWriter();
        writer.write(echo);
        writer.flush();
        writer.close();
    }
}
