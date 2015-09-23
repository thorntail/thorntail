package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment12.DependenciesType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment12.JBossDeploymentStructureDescriptor;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment12.ModuleDependencyType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.wildfly.swarm.container.util.ClassLoading.withTCCL;

/**
 * @author Bob McWhirter
 */
public class JBossDeploymentStructureAsset implements Asset{

    public JBossDeploymentStructureAsset() {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                         () -> Descriptors.create(JBossDeploymentStructureDescriptor.class));
    }

    public JBossDeploymentStructureAsset(InputStream fromStream) {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                         () -> Descriptors.importAs(JBossDeploymentStructureDescriptor.class)
                                 .fromStream(fromStream));
    }


    public void addModule(final String name, final String slot) {
        final DependenciesType dependencies = this.descriptor
                .getOrCreateDeployment()
                .getOrCreateDependencies();
        final List<ModuleDependencyType> modules = dependencies.getAllModule();
        for (ModuleDependencyType each : modules) {
            if (name.equals(each.getName()) &&
                    slot.equals(each.getSlot())) {

                //module exists
                return;
            }
        }

        dependencies.createModule()
                .name(name)
                .slot(slot);
    }

    @Override
    public InputStream openStream() {
        String output = this.descriptor.exportAsString();

        return new ByteArrayInputStream(output.getBytes());
    }

    private final JBossDeploymentStructureDescriptor descriptor;
}
