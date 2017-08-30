package org.wildfly.swarm.container.runtime.usage;

/**
 * Created by bob on 8/30/17.
 */
public interface UsageProvider {

    static UsageProvider ofString(String str) {
        return new StringUsageProvider(str);
    }

    static UsageProvider byModule() {
        return new ModuleUsageProvider();
    }

    default String getRawUsageText() throws Exception {
        return null;
    }

    class StringUsageProvider implements UsageProvider {
        public StringUsageProvider(String str) {
            this.str = str;
        }

        @Override
        public String getRawUsageText() throws Exception {
            return this.str;
        }

        private final String str;
    }
}
