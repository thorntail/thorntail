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
package org.wildfly.swarm.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public ModuleAnalyzer(File f) throws IOException {
        this(new FileInputStream(f));
    }

    public ModuleAnalyzer(Path p) throws IOException {
        this(p.toFile());
    }

    public ModuleAnalyzer(InputStream in) throws IOException {
        NodeImporter importer = new XmlDomNodeImporterImpl();
        Node node = importer.importAsNode(in, true);

        String rootName = node.getName();

        if (rootName.equals("module")) {
            this.module = new ModuleDescriptorImpl(null, node);
        }
        in.close();
    }

    public String getName() {
        if (this.module == null) {
            return "UNKNOWN";
        }
        return this.module.getName();
    }

    public String getSlot() {
        if (this.module.getSlot() == null) {
            return "main";
        }
        return this.module.getSlot();
    }

    public List<ArtifactSpec> getDependencies() {
        if (this.module == null) {
            return Collections.emptyList();
        }

        List<ArtifactType<ResourcesType<ModuleDescriptor>>> artifacts = this.module.getOrCreateResources().getAllArtifact();

        List<ArtifactSpec> dependencies = new ArrayList<>();

        String localRepo = System.getProperty("user.home") + File.separator+".m2"+File.separator+"repository";

        for (ArtifactType<ResourcesType<ModuleDescriptor>> artifact : artifacts) {
            ArtifactSpec dep = ArtifactSpec.fromMscGav(artifact.getName());

            File file = Paths.get(localRepo, dep.jarRepoPath()).toFile();
            if(!file.exists()) {
                dep.shouldGather = true;
            }  else {
                dep.file = file;
                dep.shouldGather = false;
            }
            dependencies.add(dep);
        }

        return dependencies;
    }

    private ModuleDescriptorImpl module;
}
