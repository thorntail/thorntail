/**
 * Copyright 2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.maven.plugin;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * An additional Maven dependency of the testing project. It can be a simple utility library that doesn't interact
 * with Swarm in any way, or it can be a Java EE-enabled project that will cause another Swarm fraction to be autodetected.
 */
public enum AdditionalDependency {
    NONE,
    NON_JAVA_EE,
    USING_JAVA_EE;

    public String dependencySnippet() {
        switch (this) {
            case NONE:
                return "";
            case NON_JAVA_EE:
                return "<dependency>\n" +
                        "  <groupId>com.google.guava</groupId>\n" +
                        "  <artifactId>guava</artifactId>\n" +
                        "  <version>20.0</version>\n" +
                        "</dependency>\n";
            case USING_JAVA_EE:
                return "<dependency>\n" +
                        "  <groupId>org.richfaces</groupId>\n" +
                        "  <artifactId>richfaces</artifactId>\n" +
                        "  <version>4.5.17.Final</version>\n" +
                        "</dependency>\n";
            default:
                throw new AssertionError();
        }
    }

    public Optional<String> shouldBringFraction() {
        return this == USING_JAVA_EE ? Optional.of("jsf") : Optional.empty();
    }

    public static Set<String> allPossibleFractions() {
        return Collections.singleton("jsf");
    }
}
