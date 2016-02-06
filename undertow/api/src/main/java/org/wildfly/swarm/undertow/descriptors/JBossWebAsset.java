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
package org.wildfly.swarm.undertow.descriptors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.jbossweb60.JbossWebDescriptor;

import static org.wildfly.swarm.container.util.ClassLoading.withTCCL;

/**
 * @author Bob McWhirter
 */
public class JBossWebAsset implements Asset {


    private final JbossWebDescriptor descriptor;

    private boolean rootSet = false;

    public JBossWebAsset() {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                        () -> Descriptors.create(JbossWebDescriptor.class));
    }

    public JBossWebAsset(InputStream fromStream) {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                        () -> Descriptors.importAs(JbossWebDescriptor.class)
                                .fromStream(fromStream));
    }

    public String getContextRoot() {
        return this.descriptor.getContextRoot();
    }

    public void setContextRoot(String contextRoot) {
        this.descriptor.contextRoot(contextRoot);
        rootSet = true;
    }

    public boolean isRootSet() {
        return rootSet;
    }

    @Override
    public InputStream openStream() {
        return new ByteArrayInputStream(this.descriptor.exportAsString().getBytes());
    }

}
