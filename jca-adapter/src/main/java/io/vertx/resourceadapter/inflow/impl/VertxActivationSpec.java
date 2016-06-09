package io.vertx.resourceadapter.inflow.impl;

import java.util.logging.Logger;

import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import io.vertx.resourceadapter.impl.AbstractJcaBase;
import io.vertx.resourceadapter.inflow.VertxListener;

/**
 * VertxActivationSpec
 */
@Activation(messageListeners = {VertxListener.class})
public class VertxActivationSpec extends AbstractJcaBase implements ActivationSpec {

    /**
     * Default constructor
     */
    public VertxActivationSpec() {

    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    @ConfigProperty
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * This method may be called by a deployment tool to validate the overall
     * activation configuration information provided by the endpoint deployer.
     *
     * @throws InvalidPropertyException indicates invalid configuration property settings.
     */
    public void validate() throws InvalidPropertyException {
        log.finest("validate()");
        if (this.address == null || this.address.length() == 0) {
            throw new InvalidPropertyException("Address must be specified.");
        }
    }

    /**
     * Get the resource adapter
     *
     * @return The handle
     */
    public ResourceAdapter getResourceAdapter() {
        log.finest("getResourceAdapter()");
        return ra;
    }

    /**
     * Set the resource adapter
     *
     * @param ra The handle
     */
    public void setResourceAdapter(ResourceAdapter ra) {
        log.finest("setResourceAdapter()");
        this.ra = ra;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        VertxActivationSpec other = (VertxActivationSpec) obj;
        return super.equals(other);
    }

    private static final Logger log = Logger.getLogger(VertxActivationSpec.class.getName());

    private ResourceAdapter ra;

    private String address;

}
