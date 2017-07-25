package org.wildfly.swarm.jsp.test;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.util.ServiceLoader;

@WebServlet("/services")
public class ServicesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServiceLoader<TransformerFactory> loader = ServiceLoader.load(TransformerFactory.class);
        boolean found = false;

        StringBuffer sb = new StringBuffer();
        for (TransformerFactory t : loader) {
            found = true;
            sb.append(t.getClass() + " > " + t.getClass().getClassLoader());
        }

        if (!found) {
            response.getWriter().println("No TransformerFactory could be found!");
        } else {
            response.getWriter().print(sb.toString());
        }
    }
}
