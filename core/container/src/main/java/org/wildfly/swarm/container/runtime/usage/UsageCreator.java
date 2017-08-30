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
package org.wildfly.swarm.container.runtime.usage;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by bob on 8/30/17.
 */
@ApplicationScoped
public class UsageCreator {

    @Inject
    public UsageCreator(UsageProvider provider, UsageVariableSupplier supplier) {
        this.provider = provider;
        this.supplier = supplier;
    }

    public String getUsageMessage() throws Exception {
        return replaceVariables(readRawUsage());
    }

    public String readRawUsage() throws Exception {
        return this.provider.getRawUsageText();
    }

    public String replaceVariables(String raw) throws Exception {
        if (raw == null) {
            return null;
        }

        Matcher matcher = PATTERN.matcher(raw);
        StringBuilder replaced = new StringBuilder();

        int cur = 0;

        while (matcher.find()) {
            MatchResult result = matcher.toMatchResult();

            replaced.append(raw.substring(cur, result.start(1)));

            String name = result.group(2);
            Object value = this.supplier.valueOf(name);
            if (value == null) {
                value = "${" + name + "}";
            }
            replaced.append(value);

            cur = result.end();
        }

        replaced.append(raw.substring(cur));

        return replaced.toString();
    }

    private static final Pattern PATTERN = Pattern.compile("[^\\\\]?(\\$\\{([^}]+)\\})");

    private final UsageProvider provider;

    private final UsageVariableSupplier supplier;

}
