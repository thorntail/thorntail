package io.vertx.resourceadapter.impl;

import javax.resource.spi.ConfigProperty;

/**
 * @author Lin Gao <lgao@redhat.com>
 */
public abstract class AbstractJcaBase {

    /**
     * @return the clusterPort
     */
    public Integer getClusterPort() {
        return this.vertxPlatformConfig.getClusterPort();
    }

    /**
     * @param clusterPort the clusterPort to set
     */
    @ConfigProperty
    public void setClusterPort(Integer clusterPort) {
        this.vertxPlatformConfig.setClusterPort(clusterPort);
    }

    /**
     * @return the clusterHost
     */
    public String getClusterHost() {
        return this.vertxPlatformConfig.getClusterHost();
    }

    /**
     * @param clusterHost the clusterHost to set
     */
    @ConfigProperty(defaultValue = "localhost")
    public void setClusterHost(String clusterHost) {
        this.vertxPlatformConfig.setClusterHost(clusterHost);
    }

    @ConfigProperty(defaultValue = "false")
    public void setClustered(Boolean clustered) {
        this.vertxPlatformConfig.setClustered(clustered);
    }

    public Boolean isClustered() {
        return this.vertxPlatformConfig.isClustered();
    }

    public VertxPlatformConfiguration getVertxPlatformConfig() {
        return this.vertxPlatformConfig;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((vertxPlatformConfig == null) ? 0 : vertxPlatformConfig.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractJcaBase other = (AbstractJcaBase) obj;
        if (vertxPlatformConfig == null) {
            if (other.vertxPlatformConfig != null)
                return false;
        } else if (!vertxPlatformConfig.equals(other.vertxPlatformConfig))
            return false;
        return true;
    }

    private final VertxPlatformConfiguration vertxPlatformConfig = new VertxPlatformConfiguration();

}
