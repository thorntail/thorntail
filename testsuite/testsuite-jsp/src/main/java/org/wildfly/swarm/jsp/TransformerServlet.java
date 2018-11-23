package org.wildfly.swarm.jsp;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;

@WebServlet("/transformer")
public class TransformerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        TransformerFactory factory = TransformerFactory.newInstance();

        if ("__redirected.__TransformerFactory".equals(factory.getClass().getName())) {
            response.getWriter().println(factory.toString());
        } else  {
            response.setStatus(500);
            response.getWriter().print(factory.getClass().getName());
        }
    }
}
