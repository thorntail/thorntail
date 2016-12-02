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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An additional fraction that is added in the configuration of the Swarm Maven plugin. It can either be a fraction
 * that is already present in the project, or a fraction that isn't used in the project yet.
 */
public enum AdditionalFraction {
    NONE(null),
    // this must always be present in the testing project, no matter the setup
    ALREADY_PRESENT("undertow"),
    // this must be chosen carefuly to satisfy two conditions:
    // - no other fraction that can possibly be used in the testing project can bring this
    // - it must not bring in any other fraction that can possibly be used in the testing project
    //   (except of org.wildfly.swarm:container, that is brought by all fractions)
    NOT_YET_PRESENT("msc");

    private final String value;

    AdditionalFraction(String value) {
        this.value = value;
    }

    public Optional<String> shouldBringFraction() {
        return Optional.ofNullable(value);
    }

    public static Set<String> allPossibleFractions() {
        return Stream.of(AdditionalFraction.values())
                .map(af -> af.value)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
