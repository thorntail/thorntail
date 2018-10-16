package io.thorntail.agroal.impl.opentracing;

import io.agroal.api.AgroalDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;


/**
 * Created by bob on 2/26/18.
 */
public class TracedDataSource implements DataSource, Serializable {

    /*
        public TracedDataSource(TracedLocalManagedConnectionFactory mcf, AgroalDataSource delegate) {
            this.mcf = mcf;
            this.delegate = delegate;
        }
    */
    public TraceInfo traceInfo() {
        throw new RuntimeException("Not currently supported");
//        return this.mcf;
    }

    public TracedDataSource(AgroalDataSource delegate) {
//        this.mcf = mcf;
        this.delegate = delegate;
    }

//?    public TraceInfo traceInfo() {
//        return this.mcf;
//    }

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


    private final AgroalDataSource delegate;
//    private final TracedLocalManagedConnectionFactory mcf;
}
