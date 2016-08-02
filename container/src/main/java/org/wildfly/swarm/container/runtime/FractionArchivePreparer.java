package org.wildfly.swarm.container.runtime;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.Module;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;
import org.wildfly.swarm.spi.runtime.ArchivePreparer;

/**
 * @author Bob McWhirter
 */
@Singleton
public class FractionArchivePreparer implements ArchivePreparer {

    @Inject
    @Any
    private Instance<Fraction> allFractions;

    public void prepareArchive(Archive<?> archive) {

        JARArchive jarArchive = archive.as(JARArchive.class);

        for (Fraction each : this.allFractions) {

            DeploymentModules plural = each.getClass().getAnnotation(DeploymentModules.class);

            if (plural != null) {
                DeploymentModule[] entries = plural.value();
                for (DeploymentModule entry : entries) {
                    addModule(jarArchive, entry);
                }
            } else {
                DeploymentModule entry = each.getClass().getAnnotation(DeploymentModule.class);
                if ( entry != null ) {
                    addModule( jarArchive, entry );
                }
            }
        }
    }

    protected void addModule(JARArchive archive, DeploymentModule entry) {
        String moduleName = entry.name();
        String moduleSlot = entry.slot();
        if (moduleSlot.equals("")) {
            moduleSlot = "main";
        }
        Module def = archive.addModule(moduleName, moduleSlot);
        def.withExport(entry.export());
        def.withMetaInf(entry.metaInf().toString());
    }
}
