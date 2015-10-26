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
package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class UndertowHandlersAsset implements Asset {

    private List<String[]> staticContent = new ArrayList<>();

    public void staticContent(String context, String base) {
        staticContent.add(new String[]{context, base});
    }

    @Override
    public InputStream openStream() {

        StringBuilder conf = new StringBuilder();
        for (String[] each : this.staticContent) {
            conf.append("path-prefix('" + each[0] + "') -> static-content(base='" + each[1] + "', prefix='" + each[0] + "')\n");
        }

        return new ByteArrayInputStream(conf.toString().getBytes());
    }
}
