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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author Bob McWhirter
 */
public class ServiceActivatorAsset implements Asset {

    public ServiceActivatorAsset() {

    }

    public ServiceActivatorAsset(InputStream inputStream) {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        try {
            while ((line = in.readLine()) != null) {
                addServiceActivator(line);
            }
        } catch (IOException e) {
            System.err.println("ERROR reading ServiceActivatorAsset" + e);
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void addServiceActivator(String className) {
        this.activators.add(className);
    }

    public void addServiceActivator(Class<? extends ServiceActivator> cls) {
        this.activators.add(cls.getName());
    }

    public boolean containsServiceActivator(String className) {
        return this.activators.contains(className);
    }

    @Override
    public InputStream openStream() {
        StringBuilder builder = new StringBuilder();

        for (String activator : this.activators) {
            builder.append(activator).append("\n");
        }

        return new ByteArrayInputStream(builder.toString().getBytes());
    }

    public String toString() {
        return this.activators.toString();

    }

    private List<String> activators = new ArrayList<>();
}
