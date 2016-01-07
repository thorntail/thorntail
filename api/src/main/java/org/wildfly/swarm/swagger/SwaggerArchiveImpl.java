package org.wildfly.swarm.swagger;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.msc.ServiceActivatorArchive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lance Ball
 */
public class SwaggerArchiveImpl extends AssignableBase<ArchiveBase<?>> implements SwaggerArchive {
    public static final String SERVICE_ACTIVATOR_CLASS_NAME = "org.wildfly.swarm.swagger.runtime.SwaggerActivator";
    private List<String> packageNames = new ArrayList<>();

    public SwaggerArchiveImpl(ArchiveBase<?> archive) {
        super(archive);
    }

    @Override
    public SwaggerArchive register(String... packages) {
        for(String name : packages) packageNames.add(name);
        return doRegister();
    }

    private SwaggerArchive doRegister() {
        if (!as(ServiceActivatorArchive.class).containsServiceActivator(SERVICE_ACTIVATOR_CLASS_NAME)) {
            as(ServiceActivatorArchive.class).addServiceActivator(SERVICE_ACTIVATOR_CLASS_NAME);
            as(JARArchive.class).addModule("org.wildfly.swarm.swagger", "runtime");
        }

        StringBuffer buf = new StringBuffer();
        List<String> names = getPackageNames();
        for (String name : names) {
            buf.append(name).append("\n");
        }


        as(JARArchive.class).add(new StringAsset(buf.toString()), SWAGGER_CONFIGURATION_PATH);
        return this;

    }

    public List<String> getPackageNames() {
        return packageNames;
    }
}
