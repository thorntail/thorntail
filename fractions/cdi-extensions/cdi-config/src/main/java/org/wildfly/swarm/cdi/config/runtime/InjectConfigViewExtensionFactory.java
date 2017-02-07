package org.wildfly.swarm.cdi.config.runtime;

import java.lang.reflect.Field;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.spi.api.UserSpaceExtensionFactory;
import org.wildfly.swarm.spi.api.config.ConfigView;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class InjectConfigViewExtensionFactory implements UserSpaceExtensionFactory {

    @Inject
    ConfigView configView;

    @Override
    public void configure() throws Exception {
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.cdi.config", "deployment"));
        Class<?> use = module.getClassLoader().loadClass("org.wildfly.swarm.cdi.config.deployment.InjectConfigViewExtension");
        Field field = use.getDeclaredField("configView");
        field.setAccessible(true);
        field.set(null, this.configView);
    }
}
