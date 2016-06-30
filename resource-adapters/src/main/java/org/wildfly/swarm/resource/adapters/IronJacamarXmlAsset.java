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
package org.wildfly.swarm.resource.adapters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.wildfly.swarm.config.resource.adapters.ResourceAdapter;

/**
 * @author Ralf Battenfeld
 */
public class IronJacamarXmlAsset implements Asset {
    public IronJacamarXmlAsset(final ResourceAdapter<?> ra) {
        this.ra = ra;
    }

    @Override
    public InputStream openStream() {
        return new ByteArrayInputStream(IronJacamarXmlAssetImpl.INSTANCE.transform(ra).getBytes());
    }

    private final ResourceAdapter<?> ra;
}
