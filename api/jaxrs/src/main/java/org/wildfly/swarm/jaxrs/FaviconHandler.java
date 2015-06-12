package org.wildfly.swarm.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * @author Bob McWhirter
 */
public class FaviconHandler {
    public Response toResponse(NotFoundException e) {
        if (e.getMessage().contains("favicon.ico")) {
            final InputStream in = FaviconHandler.class.getClassLoader().getResourceAsStream("wildfly-swarm-resources/favicon.ico");
            if (in != null) {
                Response.ResponseBuilder builder = Response.ok();
                builder.entity(new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        try {
                            byte[] buf = new byte[1024];
                            int numRead = -1;
                            while ((numRead = in.read(buf)) >= 0) {
                                output.write(buf, 0, numRead);
                            }
                        } finally {
                            in.close();
                            output.flush();
                        }
                    }
                });
                return builder.build();
            }
        }

        // can't handle it, rethrow.
        throw e;
    }
}
