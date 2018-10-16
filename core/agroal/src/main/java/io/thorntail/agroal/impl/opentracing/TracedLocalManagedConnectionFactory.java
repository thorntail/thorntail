package io.thorntail.agroal.impl.opentracing;

import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.thorntail.TraceMode;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by bob on 2/26/18.
 */
public class TracedLocalManagedConnectionFactory extends AgroalConnectionFactoryConfigurationSupplier implements TraceInfo {

    public TracedLocalManagedConnectionFactory() {

    }

    public void setTraceMode(String traceMode) {
        this.trace = TraceMode.valueOf(traceMode);
    }

    public TraceMode traceMode() {
        return this.trace;
    }

    @Override
    public String userName() {
        return get().principal().getName();
    }

    @Override
    public String dbInstance() {
        return this.dbInstance.updateAndGet((prev) -> {
            if (prev != null) {
                return prev;
            }

            return calculateDBInstance();
        });
    }

    private String calculateDBInstance() {
        String url = get().jdbcUrl();

        int lastSlash = url.lastIndexOf("/");
        if (lastSlash >= 0) {
            return url.substring(lastSlash + 1);
        }

        int colonLoc = url.indexOf(":", "jdbc:".length());
        if (colonLoc > 0) {
            return url.substring(colonLoc + 1);
        }

        return url;
    }

/*
    @Override
    public Object createConnectionFactory(ConnectionManager cm) throws ResourceException {
        WrapperDataSource cf = (WrapperDataSource) super.createConnectionFactory(cm);
        return new TracedDataSource(this, cf);
    }
*/


    private TraceMode trace;

    private AtomicReference<String> dbInstance = new AtomicReference<>();
}
