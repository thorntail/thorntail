package org.jboss.unimbus.datasources;

/**
 * Mode for tracing of datasource activity if {@code opentracing} is available.
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

