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
package org.wildfly.swarm.undertow.internal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.api.asset.Asset;

public class UndertowExternalMountsAsset implements Asset {

    public UndertowExternalMountsAsset() {

    }

    public UndertowExternalMountsAsset(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                externalMount(line);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading Undertow external mounts conf", ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignore) {

            }
        }
    }

    public void externalMount(String path) {
        externalMounts.add(path);
    }

    @Override
    public InputStream openStream() {
        StringBuilder conf = new StringBuilder();
        for (String each : this.externalMounts) {
            conf.append(each + "\n");
        }
        return new ByteArrayInputStream(conf.toString().getBytes());
    }

    private List<String> externalMounts = new ArrayList<>();
}
