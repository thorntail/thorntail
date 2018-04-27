package io.thorntail.datasources.impl.opentracing;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.sql.DataSource;

import org.jboss.jca.adapters.jdbc.WrapperDataSource;

/**
 * Created by bob on 2/26/18.
 */
public class TracedDataSource implements DataSource, Referenceable, Serializable {

    public TracedDataSource(TracedLocalManagedConnectionFactory mcf, WrapperDataSource delegate) {
        this.mcf = mcf;
        this.delegate = delegate;
    }

    public TraceInfo traceInfo() {
        return this.mcf;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new TracedConnection(this, delegate.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new TracedConnection(this, delegate.getConnection(username, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public void setReference(Reference reference) {
        delegate.setReference(reference);
    }

    @Override
    public Reference getReference() throws NamingException {
        return delegate.getReference();
    }


    private final WrapperDataSource delegate;
    private final TracedLocalManagedConnectionFactory mcf;
}
