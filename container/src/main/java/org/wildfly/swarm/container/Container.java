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
package org.wildfly.swarm.container;

import java.net.URL;

import javax.enterprise.inject.Vetoed;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 * @deprecated Use {@link org.wildfly.swarm.Swarm} instead.
 */
@Deprecated
@Vetoed
public class Container extends Swarm {

    public Container() throws Exception {
        super();
    }

    public Container(boolean debugBootstrap) throws Exception {
        super(debugBootstrap);
    }


    public Container(String... args) throws Exception {
        super(args);
    }

    public Container(boolean debugBootstrap, String... args) throws Exception {
        super(debugBootstrap, args);
    }

    @Override
    @Deprecated
    public Container withStageConfig(URL url) {
        return (Container) super.withStageConfig(url);
    }

    @Override
    @Deprecated
    public Container withXmlConfig(URL url) {
        return (Container) super.withXmlConfig(url);
    }

    @Override
    @Deprecated
    public Container fraction(Fraction fraction) {
        return (Container) super.fraction(fraction);
    }

    @Override
    @Deprecated
    public Container iface(String name, String expression) {
        return (Container) super.iface(name, expression);
    }

    @Override
    @Deprecated
    public Container socketBindingGroup(SocketBindingGroup group) {
        return (Container) super.socketBindingGroup(group);
    }

    @Override
    @Deprecated
    public Container start(boolean eagerlyOpen) throws Exception {
        return (Container) super.start(eagerlyOpen);
    }

    @Override
    @Deprecated
    public Container stop() throws Exception {
        return (Container) super.stop();
    }

    @Override
    @Deprecated
    public Container start() throws Exception {
        return (Container) super.start();
    }

    @Override
    @Deprecated
    public Container start(Archive<?> deployment) throws Exception {
        return (Container) super.start(deployment);
    }

    @Override
    @Deprecated
    public Container deploy() throws Exception {
        return (Container) super.deploy();
    }

    @Override
    @Deprecated
    public Container deploy(Archive<?> deployment) throws Exception {
        return (Container) super.deploy(deployment);
    }

    public Archive<?> createDefaultDeployment() throws Exception {
        return super.createDefaultDeployment();
    }

}