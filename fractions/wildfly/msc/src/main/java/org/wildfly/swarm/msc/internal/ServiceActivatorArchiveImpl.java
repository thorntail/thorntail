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
package org.wildfly.swarm.msc.internal;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.msc.ServiceActivatorArchive;

/**
 * @author Bob McWhirter
 */

public final class ServiceActivatorArchiveImpl extends AssignableBase<ArchiveBase<?>> implements ServiceActivatorArchive {

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     */
    public ServiceActivatorArchiveImpl(ArchiveBase<?> archive) {
        super(archive);
        prepareAsset();
    }

    private String path() {
        if (getArchive().getName().endsWith(".war")) {
            return "WEB-INF/classes/META-INF/services/" + ServiceActivator.class.getName();
        }

        return "META-INF/services/" + ServiceActivator.class.getName();
    }

    public ServiceActivatorAsset getAsset() {
        return this.asset;
    }

    public ServiceActivatorArchive setAsset(ServiceActivatorAsset asset) {
        this.asset = asset;
        getArchive().add(this.asset, path());
        return this;
    }

    protected void prepareAsset() {
        Node node = getArchive().get(path());
        if (node != null) {
            Asset maybeCorrect = node.getAsset();
            if (maybeCorrect instanceof ServiceActivatorAsset) {
                setAsset((ServiceActivatorAsset) maybeCorrect);
            } else {
                ServiceActivatorAsset read = new ServiceActivatorAsset(maybeCorrect.openStream());
                setAsset(read);
            }
        } else {
            setAsset(new ServiceActivatorAsset());
        }
    }

    public ServiceActivatorArchive addServiceActivator(Class<? extends ServiceActivator> cls) {
        if (getArchive().getName().endsWith(".war")) {
            getArchive().as(WebArchive.class).addClass(cls);
        } else if (getArchive().getName().endsWith(".jar")) {
            getArchive().as(JavaArchive.class).addClass(cls);
        }
        this.asset.addServiceActivator(cls);
        return this;
    }

    public ServiceActivatorArchive addServiceActivator(String className) {
        this.asset.addServiceActivator(className);
        return this;
    }

    public boolean containsServiceActivator(String className) {
        return this.asset.containsServiceActivator(className);
    }

    private ServiceActivatorAsset asset;
}


