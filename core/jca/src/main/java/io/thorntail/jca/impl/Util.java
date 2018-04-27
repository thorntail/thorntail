package io.thorntail.jca.impl;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.spec.ConfigPropertyImpl;

/**
 * Created by bob on 2/9/18.
 */
public class Util {

    public static ConfigProperty property(String name, String value) {
        return new ConfigPropertyImpl(null,
                                      str(name),
                                      str("java.lang.String"),
                                      str(value),
                                      false,
                                      false,
                                      false,
                                      name,
                                      true,
                                      null,
                                      null,
                                      null,
                                      null);

    }

    public static XsdString str(String value) {
        return new XsdString(value, null);
    }

    public static ConfigProperty duplicateProperty(ConfigProperty original, String newValue) {
        ConfigPropertyImpl replacement = new ConfigPropertyImpl(
                original.getDescriptions(),
                original.getConfigPropertyName(),
                original.getConfigPropertyType(),
                new XsdString(newValue, original.getConfigPropertyName().getId()),
                original.getConfigPropertyIgnore(),
                original.getConfigPropertySupportsDynamicUpdates(),
                original.getConfigPropertyConfidential(),
                original.getId(),
                original.isMandatory(),
                original.getAttachedClassName(),
                original.getConfigPropertyIgnoreId(),
                original.getConfigPropertySupportsDynamicUpdatesId(),
                original.getConfigPropertyConfidentialId());

        return replacement;
    }
}
