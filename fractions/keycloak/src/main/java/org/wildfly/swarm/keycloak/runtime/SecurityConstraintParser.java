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
package org.wildfly.swarm.keycloak.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wildfly.swarm.undertow.descriptors.SecurityConstraint;

class SecurityConstraintParser {

    private SecurityConstraintParser() {
    }

    /**
     * Parse a String representation of SecurityConstraint from ProjectStage and return SecurityConstraint Instance.
     *
     * <p>If, for example, swarm.keycloak.security-constraints is provided as a set of list like below</p>
     *
     * <pre>
     * swarm.keycloak.security-constraints:
     *   - url-pattern: /secret
     *     methods: [GET, POST]
     *     roles: [admin]
     * </pre>
     *
     * <p>StageConfig has it as the String like <code>{url-pattern=/secret, methods=[GET, POST], roles=[admin]}</code> .</p>
     *
     * <p>Then this method would parse the String and return SecurityConstraint instance.
     *
     * @param scAsString The String representation of SecurityConstraint from ProjectStage
     * @return The SecurityConstraint instance
     * @see SecurityConstraint
     */
    static SecurityConstraint parse(String scAsString) {
        SecurityConstraint sc;

        Matcher matcher = getMatcher(scAsString, "url-pattern=(.*?)[,}]");
        if (matcher.find()) {
            sc = new SecurityConstraint(matcher.group(1));
        } else {
            sc = new SecurityConstraint();
        }

        matcher = getMatcher(scAsString, "methods=\\[(.*?)\\]");
        if (matcher.find()) {
            sc.withMethod(trim(matcher.group(1).split(",")));
        }

        matcher = getMatcher(scAsString, "roles=\\[(.*?)\\]");
        if (matcher.find()) {
            sc.withRole(trim(matcher.group(1).split(",")));
        }

        return sc;
    }

    private static Matcher getMatcher(String scAsString, String exp) {
        Pattern pattern = Pattern.compile(exp);
        return pattern.matcher(scAsString);
    }

    private static String[] trim(String[] array) {
        String[] trimmed = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            trimmed[i] = array[i].trim();
        }

        return trimmed;
    }

}
