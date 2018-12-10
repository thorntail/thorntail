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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A Java EE technology that is used in the testing project. It always corresponds to a fraction.
 */
public enum IncludedTechnology {
    SERVLET("undertow", "org.jboss.spec.javax.servlet", "jboss-servlet-api_4.0_spec", "1.0.0.Final"),
    JAX_RS("jaxrs", "org.jboss.spec.javax.ws.rs", "jboss-jaxrs-api_2.1_spec", "1.0.1.Final", SERVLET),
    EJB("ejb", "org.jboss.spec.javax.ejb", "jboss-ejb-api_3.2_spec", "1.0.1.Final", SERVLET), // transitively depends on servlet
    // the remaining technologies are NOT supposed to be used in the testing project;
    // instead, they are listed to facilitate checking that they aren't present by some mistake
    EJB_REMOTE("ejb-remote", null, null, null),
    ;

    private final String fraction;
    private final String specificationGroupId;
    private final String specificationArtifactId;
    private final String specificationVersion;
    private final IncludedTechnology[] dependsOn;

    IncludedTechnology(String fraction,
                       String specificationGroupId, String specificationArtifactId, String specificationVersion,
                       IncludedTechnology... dependsOn) {
        this.fraction = fraction;
        this.specificationGroupId = specificationGroupId;
        this.specificationArtifactId = specificationArtifactId;
        this.specificationVersion = specificationVersion;
        this.dependsOn = dependsOn;
    }

    public String fraction() {
        return fraction;
    }

    public Set<IncludedTechnology> dependsOn() {
        return new HashSet<>(Arrays.asList(dependsOn));
    }

    public String dependencySnippet(Dependencies dependencies) {
        switch (dependencies) {
            case FRACTIONS:
                return "<dependency>\n" +
                        "  <groupId>io.thorntail</groupId>\n" +
                        "  <artifactId>" + fraction + "</artifactId>\n" +
                        "</dependency>\n";
            case JAVA_EE_APIS:
                return "<dependency>\n" +
                        "  <groupId>" + specificationGroupId + "</groupId>\n" +
                        "  <artifactId>" + specificationArtifactId + "</artifactId>\n" +
                        "  <version>" + specificationVersion + "</version>\n" +
                        "  <scope>provided</scope>\n" +
                        "</dependency>\n";
        }

        throw new IllegalArgumentException("IncludedTechnology: " + this + ", Dependencies: " + dependencies);
    }
}
