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
package org.wildfly.swarm.msc;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;

/**
 * @author Bob McWhirter
 */

public class ServiceActivatorArchiveImpl extends AssignableBase<ArchiveBase<?>> implements ServiceActivatorArchive {

    private ServiceActivatorAsset asset;

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public ServiceActivatorArchiveImpl(ArchiveBase<?> archive) {
        super(archive);

        if ( getArchive().getName().endsWith( ".war" ) ) {
            Node node = getArchive().get("WEB-INF/classes/META-INF/services/" + ServiceActivator.class.getName());
            if ( node != null ) {
                Asset maybeCorrect = node.getAsset();
                if(maybeCorrect instanceof ServiceActivatorAsset) {
                    this.asset = (ServiceActivatorAsset) maybeCorrect;
                } else {
                    this.asset = new ServiceActivatorAsset(maybeCorrect.openStream());
                }
            } else {
                this.asset = new ServiceActivatorAsset();
                getArchive().add( this.asset, "WEB-INF/classes/META-INF/services/" + ServiceActivator.class.getName() );
            }
        }  else if ( getArchive().getName().endsWith( ".jar" ) ) {
            Node node = getArchive().get("META-INF/services/" + ServiceActivator.class.getName());
            if ( node != null ) {
                this.asset = (ServiceActivatorAsset) node.getAsset();
            } else {
                this.asset = new ServiceActivatorAsset();
                getArchive().add( this.asset, "META-INF/services/" + ServiceActivator.class.getName() );
            }
        }
    }

    public ServiceActivatorArchive addServiceActivator(Class<? extends ServiceActivator> cls) {
        if ( getArchive().getName().endsWith( ".war" ) ) {
            getArchive().as(WebArchive.class).addClass( cls );
        } else if ( getArchive().getName().endsWith( ".jar" ) ) {
            getArchive().as(JavaArchive.class).addClass( cls );
        }
        this.asset.addServiceActivator( cls );
        return this;
    }

    public ServiceActivatorArchive addServiceActivator(String className) {
        this.asset.addServiceActivator( className );
        return this;
    }

    public boolean containsServiceActivator(String className) {
        return this.asset.containsServiceActivator( className );
    }

}


