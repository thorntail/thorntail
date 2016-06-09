package io.vertx.resourceadapter.impl;

import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

/**
 * VertxManagedConnectionMetaData
 *
 * @version $Revision: $
 */
public class VertxManagedConnectionMetaData implements ManagedConnectionMetaData {

    public VertxManagedConnectionMetaData() {
    }

    /**
     * Returns Product name of the underlying EIS instance connected through the
     * ManagedConnection.
     *
     * @return Product name of the EIS instance
     * @throws ResourceException Thrown if an error occurs
     */
    @Override
    public String getEISProductName() throws ResourceException {
        log.finest("getEISProductName()");
        return "Vert.x";
    }

    /**
     * Returns Product version of the underlying EIS instance connected through
     * the ManagedConnection.
     *
     * @return Product version of the EIS instance
     * @throws ResourceException Thrown if an error occurs
     */
    @Override
    public String getEISProductVersion() throws ResourceException {
        log.finest("getEISProductVersion()");
        return null;
    }

    /**
     * Returns maximum limit on number of active concurrent connections
     *
     * @return Maximum limit for number of active concurrent connections
     * @throws ResourceException Thrown if an error occurs
     */
    @Override
    public int getMaxConnections() throws ResourceException {
        log.finest("getMaxConnections()");
        return 0;
    }

    /**
     * Returns name of the user associated with the ManagedConnection instance
     *
     * @return Name of the user
     * @throws ResourceException Thrown if an error occurs
     */
    @Override
    public String getUserName() throws ResourceException {
        log.finest("getUserName()");
        return "vertx-user";
    }

    private static final Logger log = Logger.getLogger(VertxManagedConnectionMetaData.class.getName());

}
