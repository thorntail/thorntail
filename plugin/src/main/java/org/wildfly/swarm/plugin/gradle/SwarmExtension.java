package org.wildfly.swarm.plugin.gradle;

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

    public SwarmExtension() {

    }

    public void setMainClassName(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMainClassName() {
        return this.mainClass;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getPortOffset() {
        return portOffset;
    }

    public void setPortOffset(Integer portOffset) {
        this.portOffset = portOffset;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
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
