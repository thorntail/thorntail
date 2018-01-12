package org.jboss.unimbus.config.mp;

class SystemEnvironmentConfigSource extends MapConfigSource {

    SystemEnvironmentConfigSource() {
        super("system environment", System.getenv());
    }
}
