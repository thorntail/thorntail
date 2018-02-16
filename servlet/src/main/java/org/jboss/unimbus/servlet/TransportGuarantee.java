package org.jboss.unimbus.servlet;

/**
 * Transport guarantee for a security constraint.
 */
public enum TransportGuarantee {
    /**
     * No guarantee.
     */
    NONE,
    /**
     * Confidential transport.
     */
    CONFIDENTIAL,
}
