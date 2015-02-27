package modules.system.layers.base.org.jboss.as.server.main;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.wildfly.embedded.ArtifactLoaderFactory;
import org.wildfly.embedded.modules.BaseModule;
import org.wildfly.embedded.modules.ModuleDependency;

/**
 * @author Bob McWhirter
 */
public class Module extends BaseModule {

    public Module() {
        super("org.jboss.as.server");

        artifact("org.wildfly.core:wildfly-server:1.0.0.Beta1-SNAPSHOT");

        module("javax.api");
        module("org.jboss.staxmapper");
        module("org.jboss.common-beans").services(ModuleDependency.Services.EXPORT).optional();
        module("org.jboss.dmr");
        module("org.jboss.invocation");
        module("org.jboss.jandex");
        module("org.jboss.marshalling");
        module("org.jboss.marshalling.river").services(ModuleDependency.Services.IMPORT);
        module("org.jboss.modules");
        module("org.jboss.msc").export();
        module("org.jboss.logging");
        module("org.jboss.logmanager").services(ModuleDependency.Services.IMPORT);
        module("org.jboss.log4j.logmanager");
        module("org.jboss.remoting");
        module("org.jboss.sasl");
        module("org.jboss.stdio");
        module("org.jboss.threads");
        module("org.jboss.vfs").services(ModuleDependency.Services.IMPORT);
        module("org.jboss.as.controller");
        module("org.jboss.as.deployment-repository");
        module("org.jboss.as.domain-http-interface");
        module("org.jboss.as.domain-management");
        module("org.jboss.as.jmx").services(ModuleDependency.Services.IMPORT);
        module("org.jboss.as.network");
        module("org.jboss.as.platform-mbean");
        module("org.jboss.as.process-controller");
        module("org.jboss.as.protocol");
        module("org.jboss.as.remoting");
        module("org.wildfly.security.manager").services(ModuleDependency.Services.IMPORT);
        module("org.jboss.as.security").services(ModuleDependency.Services.IMPORT).optional();
        module("org.jboss.as.version");
        module("org.picketbox").optional();
        module("io.undertow.core");
    }
}
