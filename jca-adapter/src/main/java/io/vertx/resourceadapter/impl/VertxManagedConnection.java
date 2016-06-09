package io.vertx.resourceadapter.impl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import io.vertx.core.Vertx;
import io.vertx.resourceadapter.VertxEventBus;

/**
 * VertxManagedConnection
 */
public class VertxManagedConnection implements ManagedConnection, VertxHolder {

    public VertxManagedConnection(VertxManagedConnectionFactory mcf, Vertx vertx)
            throws ResourceException {
        this.mcf = mcf;
        this.vertx = vertx;
        this.logwriter = null;
        this.vertxConn = null;
        VertxPlatformFactory.instance().addVertxHolder(this);
    }

    public VertxManagedConnectionFactory getManagementConnectionFactory() {
        return this.mcf;
    }

    @Override
    public Vertx getVertx() {
        return this.vertx;
    }

    /**
     * Creates a new connection handle for the underlying physical connection
     * represented by the ManagedConnection instance.
     *
     * @param subject       Security context as JAAS subject
     * @param cxRequestInfo ConnectionRequestInfo instance
     * @return generic Object instance representing the connection handle.
     * @throws ResourceException generic exception if operation fails
     */
    public Object getConnection(Subject subject,
                                ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        log.finest("getConnection()");
        this.vertxConn = new VertxConnectionImpl(this);
        return vertxConn;
    }

    ;

    /**
     * Used by the container to change the association of an application-level
     * connection handle with a ManagedConneciton instance.
     *
     * @param connection Application-level connection handle
     * @throws ResourceException generic exception if operation fails
     */
    public void associateConnection(Object connection) throws ResourceException {
        log.finest("associateConnection()");

        if (connection == null)
            throw new ResourceException("Null connection handle");

        if (!(connection instanceof VertxConnectionImpl))
            throw new ResourceException("Wrong connection handle");

        this.vertxConn = (VertxConnectionImpl) connection;
    }

    /**
     * Application server calls this method to force any cleanup on the
     * ManagedConnection instance.
     *
     * @throws ResourceException generic exception if operation fails
     */
    public void cleanup() throws ResourceException {
        // there is no client-specific properties
        log.finest("cleanup()");
    }

    /**
     * Destroys the physical connection to the underlying resource manager.
     *
     * @throws ResourceException generic exception if operation fails
     */
    public void destroy() throws ResourceException {
        log.finest("destroy()");
        VertxPlatformFactory.instance().removeVertxHolder(this);
    }

    /**
     * Adds a connection event listener to the ManagedConnection instance.
     *
     * @param listener A new ConnectionEventListener to be registered
     */
    public void addConnectionEventListener(ConnectionEventListener listener) {
        log.finest("addConnectionEventListener()");
        if (listener == null)
            throw new IllegalArgumentException("Listener is null");
        listeners.add(listener);
    }

    /**
     * Removes an already registered connection event listener from the
     * ManagedConnection instance.
     *
     * @param listener already registered connection event listener to be removed
     */
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        log.finest("removeConnectionEventListener()");
        if (listener == null)
            throw new IllegalArgumentException("Listener is null");
        listeners.remove(listener);
    }

    /**
     * Gets the log writer for this ManagedConnection instance.
     *
     * @return Character output stream associated with this Managed-Connection
     * instance
     * @throws ResourceException generic exception if operation fails
     */
    public PrintWriter getLogWriter() throws ResourceException {
        log.finest("getLogWriter()");
        return logwriter;
    }

    /**
     * Sets the log writer for this ManagedConnection instance.
     *
     * @param out Character Output stream to be associated
     * @throws ResourceException generic exception if operation fails
     */
    public void setLogWriter(PrintWriter out) throws ResourceException {
        log.finest("setLogWriter()");
        logwriter = out;
    }

    /**
     * Returns an <code>javax.resource.spi.LocalTransaction</code> instance.
     *
     * @return LocalTransaction instance
     * @throws ResourceException generic exception if operation fails
     */
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException("getLocalTransaction() not supported");
    }

    /**
     * Returns an <code>javax.transaction.xa.XAresource</code> instance.
     *
     * @return XAResource instance
     * @throws ResourceException generic exception if operation fails
     */
    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException("getXAResource() not supported");
    }

    /**
     * Gets the metadata information for this connection's underlying EIS resource
     * manager instance.
     *
     * @return ManagedConnectionMetaData instance
     * @throws ResourceException generic exception if operation fails
     */
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        log.finest("getMetaData()");
        return new VertxManagedConnectionMetaData();
    }

    /**
     * Close handle
     *
     * @param handle The handle
     */
    void closeHandle(VertxConnectionImpl handle) {
        ConnectionEvent event = new ConnectionEvent(this,
                                                    ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(handle);
        for (ConnectionEventListener cel : listeners) {
            cel.connectionClosed(event);
        }
    }

    VertxEventBus getVertxEventBus() {
        return new WrappedEventBus(vertx.eventBus());
    }

    private static final Logger log = Logger.getLogger(VertxManagedConnection.class.getName());

    private final VertxManagedConnectionFactory mcf;

    private final List<ConnectionEventListener> listeners
            = Collections.synchronizedList(new ArrayList<>(1));

    private final Vertx vertx;

    private PrintWriter logwriter;

    private VertxConnectionImpl vertxConn;

}
