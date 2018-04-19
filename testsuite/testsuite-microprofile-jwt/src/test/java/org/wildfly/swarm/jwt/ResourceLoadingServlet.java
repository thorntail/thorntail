package org.wildfly.swarm.jwt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/signer-key")
public class ResourceLoadingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        InputStream inputStream = ResourceLoadingServlet.class.getClassLoader().getResourceAsStream("/META-INF/MP-JWT-SIGNER");
        if (null == inputStream) {
            throw new RuntimeException("Failed to load /META-INF/MP-JWT-SIGNER");
        }

        byte[] key = readFully(inputStream);

        response.getWriter().print(key.length);
        response.setStatus(200);
    }

    public static byte[] readFully(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)  {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }
}
