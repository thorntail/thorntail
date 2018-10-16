package io.thorntail.agroal.impl.opentracing;

import io.opentracing.Scope;

import java.sql.SQLException;

import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.thorntail.TraceMode;

/**
 * Created by bob on 2/27/18.
 */
public interface TraceInfo {
    TraceMode traceMode();

    String userName();

    String dbInstance();

    default <R> R trace(String operationName, String sql, TraceableSQL<R> code) throws SQLException {
        if (traceMode() == TraceMode.OFF) {
            return code.execute();
        }
        Tracer tracer = GlobalTracer.get();
        // skip if there is no active parent
        if (traceMode() == TraceMode.ACTIVE && tracer.activeSpan() == null) {
            return code.execute();
        }

        Tracer.SpanBuilder builder = tracer.buildSpan(operationName)
            .withTag(Tags.DB_TYPE.getKey(), "sql")
            .withTag(Tags.DB_USER.getKey(), userName());

        if (sql != null) {
            builder.withTag(Tags.DB_STATEMENT.getKey(), sql);
        }
        if (dbInstance() != null ) {
            builder.withTag(Tags.DB_INSTANCE.getKey(), dbInstance());
        }

        try (Scope scope = builder.startActive(true)) {
            return code.execute();
        }
    }

}
