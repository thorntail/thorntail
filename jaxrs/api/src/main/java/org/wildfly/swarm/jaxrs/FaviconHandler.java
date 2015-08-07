package org.wildfly.swarm.jaxrs;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * @author Bob McWhirter
 */
public class FaviconHandler {
    public Response toResponse(NotFoundException e) {
        if (e.getMessage().contains("favicon.ico")) {
            try {
                Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.undertow", "runtime"));
                ClassLoader cl = module.getClassLoader();
                final InputStream in = cl.getResourceAsStream("favicon.ico");
                if (in != null) {
                    Response.ResponseBuilder builder = Response.ok();
                    builder.entity( in );
                    return builder.build();
                }
            } catch (ModuleLoadException e1) {
                throw e;
            }
        }

        // can't handle it, rethrow.
        throw e;
    }
}
