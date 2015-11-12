package org.wildfly.swarm.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.shrinkwrap.descriptor.api.jbossmodule13.ArtifactType;
import org.jboss.shrinkwrap.descriptor.api.jbossmodule13.ModuleDescriptor;
import org.jboss.shrinkwrap.descriptor.api.jbossmodule13.ResourcesType;
import org.jboss.shrinkwrap.descriptor.impl.jbossmodule13.ModuleDescriptorImpl;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.jboss.shrinkwrap.descriptor.spi.node.NodeImporter;
import org.jboss.shrinkwrap.descriptor.spi.node.dom.XmlDomNodeImporterImpl;

/**
 * @author Bob McWhirter
 */
public class ModuleAnalyzer {

    private ModuleDescriptorImpl module;

    public ModuleAnalyzer(InputStream in) {
        NodeImporter importer = new XmlDomNodeImporterImpl();
        Node node = importer.importAsNode(in, true);

        String rootName = node.getName();

        if ( rootName.equals( "module" ) ) {
            this.module = new ModuleDescriptorImpl( null, node );
        }
    }

    public List<ArtifactSpec> getDependencies() throws IOException {
        if ( this.module == null ) {
            return Collections.emptyList();
        }

        List<ArtifactType<ResourcesType<ModuleDescriptor>>> artifacts = this.module.getOrCreateResources().getAllArtifact();

        List<ArtifactSpec> dependencies = new ArrayList<>();

        for (ArtifactType<ResourcesType<ModuleDescriptor>> artifact : artifacts) {
            ArtifactSpec dep = ArtifactSpec.fromMscGav(artifact.getName());
            dep.shouldGather = true;
            dependencies.add( dep );
        }

        return dependencies;
    }
}
