package org.wildfly.swarm.jaxrs;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.undertow.DefaultWarDeployment;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class DefaultJAXRSWarDeployment extends DefaultWarDeployment {

    public DefaultJAXRSWarDeployment(Container container) throws IOException, ModuleLoadException {
        this(container, null);
    }

    public DefaultJAXRSWarDeployment(Container container, String contextPath) throws IOException, ModuleLoadException {
        super(container, contextPath);
    }

    protected void addExceptionMapper() {
        try {
            this.archive.add(new ByteArrayAsset(FaviconExceptionMapperFactory.create()), "WEB-INF/classes/org/wildfly/swarm/generated/FaviconExceptionMapper.class");
            this.archive.addClass(FaviconHandler.class);
            this.structureModules.add("org.jboss.modules");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public WebArchive getArchive(boolean finalize) {
        if (finalize) {
            addExceptionMapper();
        }
        return super.getArchive(finalize);
    }
}
