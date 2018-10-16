package io.thorntail.agroal.impl.opentracing;

import java.sql.SQLException;

/**
 * Created by bob on 2/27/18.
 */
public interface TraceableSQL<T> {
    T execute() throws SQLException;
}
