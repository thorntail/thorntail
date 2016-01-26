/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.jaxrs;

import java.io.InputStream;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

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
                    builder.entity(in);
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
