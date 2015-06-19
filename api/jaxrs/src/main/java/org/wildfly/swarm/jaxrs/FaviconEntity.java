package org.wildfly.swarm.jaxrs;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Bob McWhirter
 */
public class FaviconEntity implements StreamingOutput {

    private final InputStream in;

    public FaviconEntity(InputStream in) {
        this.in = in;
    }

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
}
