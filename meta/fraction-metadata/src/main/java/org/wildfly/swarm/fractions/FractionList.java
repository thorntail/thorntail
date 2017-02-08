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
package org.wildfly.swarm.fractions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Bob McWhirter
 */
public class FractionList {

    private final FractionListParser fractionListParser;

    private static final AtomicReference<FractionList> INSTANCE = new AtomicReference<>();

    public static FractionList get() {
        return INSTANCE.updateAndGet(old -> old != null ? old : new FractionList());
    }

    private FractionList() {
        try (InputStream fractionJsonStream = getClass().getClassLoader().getResourceAsStream("fraction-list.json");
             InputStream packageSpecStream = getClass().getClassLoader()
                     .getResourceAsStream("org/wildfly/swarm/fractionlist/fraction-packages.properties")) {
            this.fractionListParser = new FractionListParser(fractionJsonStream, packageSpecStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<FractionDescriptor> getFractionDescriptors() {
        return fractionListParser.getFractionDescriptors();
    }

    public FractionDescriptor getFractionDescriptor(final String groupId, final String artifactId) {
        return this.fractionListParser.getFractionDescriptor(groupId, artifactId);
    }
}
