package org.jboss.unimbus.servlet;

/**
 * Semantic for empty roles in a security constraint.
 */
public enum EmptyRoleSemantic {
    /**
     * Permit all
     */
    PERMIT,

    /**
     * Deny all
     */
    DENY,
}
