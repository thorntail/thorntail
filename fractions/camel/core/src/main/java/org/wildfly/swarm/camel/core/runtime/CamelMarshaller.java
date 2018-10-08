package org.wildfly.swarm.camel.core.runtime;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.camel.core.CamelFraction;
import org.wildfly.swarm.spi.runtime.CustomMarshaller;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.modules.Module.getBootModuleLoader;

/**
 * Created by bob on 6/6/17.
 */
@ApplicationScoped
public class CamelMarshaller implements CustomMarshaller {

    @Inject
    CamelFraction fraction;

    @Override
    public void marshal(List<ModelNode> list) {
        PathAddress subsystem = PathAddress.pathAddress("subsystem", "camel");

        Map<String, String> contexts = fraction.contexts();

        for (String key : contexts.keySet()) {
            String path = contexts.get(key);
            try {
                String value = read(path);
                PathAddress context = subsystem.append("context", key);
                ModelNode node = new ModelNode();
                node.get(OP).set(ADD);
                node.get(OP_ADDR).set(context.toModelNode());
                node.get(VALUE).set(value);
                list.add(node);
            } catch (ModuleLoadException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected String read(String path) throws ModuleLoadException, IOException {
        Module app = getBootModuleLoader().loadModule("thorntail.application");

        StringBuilder str = new StringBuilder();

        try (Reader in = new InputStreamReader(app.getClassLoader().getResourceAsStream(path))) {
            char[] chars = new char[1024];
            int numRead = 0;
            while ((numRead = in.read(chars)) >= 0) {
                str.append(chars, 0, numRead);
            }
        }

        return str.toString();
    }

}
