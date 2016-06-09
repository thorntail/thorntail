package io.vertx.resourceadapter.impl;

import java.io.Serializable;

/**
 * VertxPlatformConfiguration is used to create an embedded Vertx Platform.
 *
 * @author Lin Gao <lgao@redhat.com>
 */
public class VertxPlatformConfiguration implements Serializable {

    /**
     * @return the timeout
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Long timeout) {
        if (timeout >= 0) {
            this.timeout = timeout;
        }
    }

    @Override
    public String toString() {
        String str = getVertxPlatformIdentifier();
        if (timeout != null) {
            str += ":timeout[" + timeout + "]";
        }
        return str;
    }

    /**
     * Currently only for host:port keypair.
     */
    public String getVertxPlatformIdentifier() {
        return getClusterHost() + ":" + getClusterPort();
    }

    /**
     * @return the clusterPort
     */
    public Integer getClusterPort() {
        return (clusterPort == null) ? Integer.valueOf(0) : clusterPort;
    }

    /**
     * @param clusterPort the clusterPort to set
     */
    public void setClusterPort(Integer clusterPort) {
        if (clusterPort != null) {
            this.clusterPort = clusterPort;
        }
    }

    /**
     * @return the clusterHost
     */
    public String getClusterHost() {
        return clusterHost;
    }

    /**
     * @param clusterHost the clusterHost to set
     */
    public void setClusterHost(String clusterHost) {
        if (clusterHost != null) {
            this.clusterHost = clusterHost.trim();
        }
    }

    public boolean isClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((clusterHost == null) ? 0 : clusterHost.hashCode());
        result = prime * result
                + ((clusterPort == null) ? 0 : clusterPort.hashCode());
        result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
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
        VertxPlatformConfiguration other = (VertxPlatformConfiguration) obj;
        if (clusterHost == null) {
            if (other.clusterHost != null)
                return false;
        } else if (!clusterHost.equals(other.clusterHost))
            return false;
        if (clusterPort == null) {
            if (other.clusterPort != null)
                return false;
        } else if (!clusterPort.equals(other.clusterPort))
            return false;
        if (timeout == null) {
            if (other.timeout != null)
                return false;
        } else if (!timeout.equals(other.timeout)) {
            return false;
        } else if (clustered != other.isClustered()) {
            return false;
        }

        return true;
    }

    private static final long serialVersionUID = -2647099599010357452L;

    private Integer clusterPort = Integer.valueOf(0);

    private String clusterHost = "localhost";

    private boolean clustered;

    /**
     * Timeout in milliseconds waiting for the Vert.x starts up. Default to 30000,
     * 30 seconds
     **/
    private Long timeout = 30000L;

}
