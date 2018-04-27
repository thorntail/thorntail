package io.thorntail.test.impl;

import java.util.Collections;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Created by bob on 1/23/18.
 */
public class EphemeralPortsConfigSource implements ConfigSource {

    public static final EphemeralPortsConfigSource INSTANCE = new EphemeralPortsConfigSource();

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public String getValue(String propertyName) {
        if ( propertyName.equals( "web.primary.port" ) && this.primaryIsEphemeral ) {
            return "0";
        }
        if ( propertyName.equals( "web.management.port" ) && this.managementIsEphemeral ) {
            return "0";
        }
        return null;
    }

    @Override
    public String getName() {
        return "test-ephemeral-ports";
    }

    @Override
    public int getOrdinal() {
        return Integer.MAX_VALUE;
    }

    public void setPrimaryIsEphemeral(boolean primaryIsEphemeral) {
        this.primaryIsEphemeral = primaryIsEphemeral;
    }

    public void setManagementIsEphemeral(boolean managementIsEphemeral) {
        this.managementIsEphemeral = managementIsEphemeral;
    }

    public void reset() {
        this.primaryIsEphemeral = false;
        this.managementIsEphemeral = false;
    }

    private boolean primaryIsEphemeral = false;
    private boolean managementIsEphemeral = false;
}
