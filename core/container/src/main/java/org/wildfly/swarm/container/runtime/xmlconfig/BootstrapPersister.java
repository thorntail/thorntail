/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.container.runtime.xmlconfig;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ProcessType;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.controller.RunningModeControl;
import org.jboss.as.controller.extension.ExtensionRegistry;
import org.jboss.as.controller.extension.RuntimeHostControllerInfoAccessor;
import org.jboss.as.controller.parsing.Namespace;
import org.jboss.as.controller.persistence.ConfigurationPersistenceException;
import org.jboss.as.controller.persistence.ConfigurationPersister;
import org.jboss.as.controller.persistence.ExtensibleConfigurationPersister;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.persistence.XmlConfigurationPersister;
import org.jboss.as.server.parsing.StandaloneXml;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.staxmapper.XMLElementWriter;

/**
 * Bootstrap from a precomputed set of operations and then switch to an XML based storage model.
 * In the later stages the XML config will be store in the java.io.tmpdir as <code>swarm-config-UUID.xml</code>.
 * This step leverages the default Wildfly components for persistence, in particular the {@link XmlConfigurationPersister}
 *
 * @author Heiko Braun
 * @since 14/09/16
 */
public class BootstrapPersister implements ExtensibleConfigurationPersister {

    private final File configurationFile;

    private final XmlConfigurationPersister delegate;

    private BootstrapConfiguration bootstrapConfig;

    public BootstrapPersister(BootstrapConfiguration bootstrapConfig, File configurationFile) {
        this.bootstrapConfig = bootstrapConfig;
        this.configurationFile = configurationFile;
        this.delegate = createDelegate(configurationFile);
    }

    @Override
    public PersistenceResource store(ModelNode model, Set<PathAddress> affectedAddresses) throws ConfigurationPersistenceException {
        return delegate.store(model, affectedAddresses);
    }

    @Override
    public void marshallAsXml(ModelNode model, OutputStream output) throws ConfigurationPersistenceException {
        delegate.marshallAsXml(model, output);
    }

    @Override
    public List<ModelNode> load() throws ConfigurationPersistenceException {
        if (!configurationFile.exists()) {
            return bootstrapConfig.get();
        } else {
            return delegate.load();
        }
    }

    @Override
    public void successfulBoot() throws ConfigurationPersistenceException {

    }

    @Override
    public SnapshotInfo listSnapshots() {
        return ConfigurationPersister.NULL_SNAPSHOT_INFO;
    }

    @Override
    public void deleteSnapshot(String name) {

    }

    @Override
    public void registerSubsystemWriter(String name, XMLElementWriter<SubsystemMarshallingContext> writer) {
        delegate.registerSubsystemWriter(name, writer);
    }

    @Override
    public void registerSubsystemWriter(String name, Supplier<XMLElementWriter<SubsystemMarshallingContext>> supplier) {
        delegate.registerSubsystemWriter(name, supplier);
    }

    @Override
    public void unregisterSubsystemWriter(String name) {
        delegate.unregisterSubsystemWriter(name);
    }

    private XmlConfigurationPersister createDelegate(File configFile) {

        QName rootElement = new QName(Namespace.CURRENT.getUriString(), "server");
        ExtensionRegistry extensionRegistry = new ExtensionRegistry(
                ProcessType.SELF_CONTAINED,
                new RunningModeControl(RunningMode.NORMAL),
                null,
                null,
                null,
                RuntimeHostControllerInfoAccessor.SERVER
        );
        StandaloneXml parser = new StandaloneXml(Module.getBootModuleLoader(), Executors.newSingleThreadExecutor(), extensionRegistry);

        XmlConfigurationPersister persister = new XmlConfigurationPersister(
                configFile, rootElement, parser, parser, false
        );

        return persister;

    }
}
