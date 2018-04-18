package io.thorntail.datasources.impl.opentracing;

import java.io.PrintWriter;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * Created by bob on 2/26/18.
 */
public class TracedManagedConnection implements ManagedConnection {

    public TracedManagedConnection(ManagedConnection delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return delegate.getConnection(subject, cxRequestInfo);
    }

    @Override
    public void destroy() throws ResourceException {
        delegate.destroy();
    }

    @Override
    public void cleanup() throws ResourceException {
        delegate.cleanup();
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        delegate.associateConnection(connection);
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        delegate.addConnectionEventListener(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        delegate.removeConnectionEventListener(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return delegate.getXAResource();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return delegate.getLocalTransaction();
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return delegate.getMetaData();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        delegate.setLogWriter(out);
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return delegate.getLogWriter();
    }

    private final ManagedConnection delegate;
}
