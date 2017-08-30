package org.wildfly.swarm.container.runtime.usage;

import java.util.Properties;

/**
 * Created by bob on 8/30/17.
 */
public interface UsageVariableSupplier {
    Object valueOf(String name) throws Exception;

    static UsageVariableSupplier ofProperties(Properties props) {
        return new PropertiesUsageVariableSupplier(props);
    }

    class PropertiesUsageVariableSupplier implements UsageVariableSupplier {

        PropertiesUsageVariableSupplier(Properties props) {
            this.props = props;
        }

        @Override
        public Object valueOf(String name) throws Exception {
            return this.props.getProperty(name);
        }

        private final Properties props;
    }
}
