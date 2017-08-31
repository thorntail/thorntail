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
package org.wildfly.swarm.fractions;

/**
 * This enum indicates the Fraction stability as declared in the Fraction's build descriptor
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public enum FractionStability {

    DEPRECATED("Expect no more changes. Avoid using this fraction."),
    EXPERIMENTAL("Expect the unexpected. Please provide feedback on API and your use-case."),
    UNSTABLE("Expect patches and features, possible API changes."),
    STABLE("Expect patches, possible features additions."),
    FROZEN("Expect only patches. Please do not make feature requests."),
    LOCKED("Expect no changes, except serious bugs. Please do not make feature requests.");

    private final String description;

    private FractionStability(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
