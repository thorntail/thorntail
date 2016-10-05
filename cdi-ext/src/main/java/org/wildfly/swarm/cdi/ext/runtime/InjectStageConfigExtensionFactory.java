package org.wildfly.swarm.cdi.ext.runtime;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.UserSpaceExtensionFactory;

/**
 * @author Bob McWhirter
 */
public class InjectStageConfigExtensionFactory implements UserSpaceExtensionFactory {

    @Inject
    StageConfig stageConfig;

    @Override
    public void configure() throws Exception {
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.cdi.ext", "deployment"));
        Class<?> use = module.getClassLoader().loadClass("org.wildfly.swarm.cdi.ext.deployment.InjectStageConfigExtension");
        Field field = use.getDeclaredField("stageConfig");
        field.setAccessible(true);
        field.set(null, this.stageConfig);
    }
}
