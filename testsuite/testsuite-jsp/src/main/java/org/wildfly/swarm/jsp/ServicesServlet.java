package org.wildfly.swarm.jsp;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ServiceLoader<TransformerFactory> loader = ServiceLoader.load(TransformerFactory.class);
        boolean found = false;

        StringBuilder sb = new StringBuilder();
        for (TransformerFactory t : loader) {
            found = true;
            sb.append(t.getClass().getName()).append(" > ").append(t.getClass().getClassLoader());
        }

        if (!found) {
            response.getWriter().println("No TransformerFactory could be found!");
        } else {
            response.getWriter().print(sb.toString());
        }
    }
}
