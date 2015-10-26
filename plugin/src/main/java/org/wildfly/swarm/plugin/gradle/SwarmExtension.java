/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.plugin.gradle;

import groovy.lang.Closure;
import groovy.util.ConfigObject;

import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class SwarmExtension {
    private String mainClass;
    private Integer httpPort;
    private Integer portOffset;
    private String bindAddress;
    private String contextPath;
    private Boolean bundleDependencies;

    private Properties properties = new Properties();

    public SwarmExtension() {

    }

    public void properties(Closure<Properties> closure) {
        ConfigObject config = new ConfigObject();
        closure.setResolveStrategy(Closure.DELEGATE_ONLY);
        closure.setDelegate(config);
        closure.call();
        config.flatten(this.properties);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setMainClassName(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMainClassName() {
        return this.mainClass;
    }

    public void setHttpPort(Integer httpPort) {
        System.err.println( "'httpPort' is deprecated, please use 'jboss.http.port' within 'properties'");
        this.properties.setProperty( "jboss.http.port", httpPort.toString());
    }

    public void setPortOffset(Integer portOffset) {
        System.err.println( "'portOffset' is deprecated, please use 'jboss.port.offset' within 'properties'");
        this.properties.setProperty( "jboss.port.offset", portOffset.toString());
    }

    public void setBindAddress(String bindAddress) {
        System.err.println( "'bindAddress' is deprecated, please use 'jboss.bind.address' within 'properties'");
        this.properties.setProperty( "jboss.bind.address", bindAddress.toString());
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Boolean getBundleDependencies() {
        return bundleDependencies;
    }

    public void setBundleDependencies(Boolean bundleDependencies) {
        this.bundleDependencies = bundleDependencies;
    }
}
