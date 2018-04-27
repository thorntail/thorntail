package io.thorntail;

/**
 * Mode for tracing of activity if {@code opentracing} is available.
 */
public enum TraceMode {
    /**
     * Do not trace this datasource.
     */
    OFF,
    /**
     * Trace all activity for this datasource.
     */
    ALWAYS,
    /**
     * Only trace if there's already an active span.
     */
    ACTIVE;


}

