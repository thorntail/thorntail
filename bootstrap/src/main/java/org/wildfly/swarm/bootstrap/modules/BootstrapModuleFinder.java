/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.*;
import org.wildfly.swarm.bootstrap.util.Layout;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmBootstrapConf;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashSet;

/**
 * Module-finder used only for loading the first set of jars when run in an fat-jar scenario.
 *
 * @author Bob McWhirter
 */
public class BootstrapModuleFinder extends AbstractSingleModuleFinder {

    public static final String MODULE_NAME = "org.wildfly.swarm.bootstrap";

    public BootstrapModuleFinder() {
        super(MODULE_NAME);
    }

    @Override
    public void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException {

        try {
            if (Layout.getInstance().isUberJar()) {
                handleWildFlySwarmBootstrapConf(builder);
            }

            builder.addDependency(DependencySpec.createLocalDependencySpec());
            builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.modules")));
            builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.msc")));
            builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.shrinkwrap")));
            builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("javax.api")));
            HashSet<String> paths = new HashSet<String>();
            paths.add( "org/wildfly/swarm/bootstrap/util");
            builder.addDependency(DependencySpec.createSystemDependencySpec( paths, true ) );
        } catch (IOException e) {
            throw new ModuleLoadException(e);
        } catch (URISyntaxException e) {
            throw new ModuleLoadException(e);
        }
    }

    protected void handleWildFlySwarmBootstrapConf(ModuleSpec.Builder builder) throws IOException {
        InputStream bootstrapTxt = getClass().getClassLoader().getResourceAsStream(WildFlySwarmBootstrapConf.CLASSPATH_LOCATION);

        if (bootstrapTxt != null) {
            WildFlySwarmBootstrapConf conf = new WildFlySwarmBootstrapConf( bootstrapTxt );
            conf.apply( builder );
        }
    }
}
