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
