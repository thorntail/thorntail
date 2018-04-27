package io.thorntail.servlet;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.annotation.HttpMethodConstraint;

/**
 * HTTP method constraint descriptor.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class HttpMethodConstraintMetaData {

    /**
     * Construct.
     *
     * <p>This instance is invalid until {@link #setMethod(String)} is used to provide the HTTP method.</p>
     */
    public HttpMethodConstraintMetaData() {
        this.emptyRoleSemantic = EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = TransportGuarantee.NONE;
    }

    /**
     * Construct.
     *
     * @param method The HTTP method.
     */
    public HttpMethodConstraintMetaData(String method) {
        this.method = method;
        this.emptyRoleSemantic = EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = TransportGuarantee.NONE;
    }

    /**
     * Construct.
     *
     * @param anno The underlying {@link HttpMethodConstraint} to use for initial configuration.
     */
    public HttpMethodConstraintMetaData(HttpMethodConstraint anno) {
        this.method = anno.value();

        switch (anno.emptyRoleSemantic()) {
            case PERMIT:
                this.emptyRoleSemantic = EmptyRoleSemantic.PERMIT;
                break;
            case DENY:
                this.emptyRoleSemantic = EmptyRoleSemantic.DENY;
                break;
        }

        for (String role : anno.rolesAllowed()) {
            addRoleAllowed(role);
        }

        switch (anno.transportGuarantee()) {
            case NONE:
                this.transportGuarantee = TransportGuarantee.NONE;
                break;
            case CONFIDENTIAL:
                this.transportGuarantee = TransportGuarantee.CONFIDENTIAL;
                break;
        }
    }

    /**
     * Set the HTTP method.
     *
     * @param method The HTTP method.
     * @return This meta-data object.
     */
    public HttpMethodConstraintMetaData setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Retrieve the HTTP method.
     *
     * @return The HTTP method.
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Set the empty-role semantic.
     *
     * @param emptyRoleSemantic The empty-role semantic.
     * @return This meta-data object.
     */
    public HttpMethodConstraintMetaData setEmptyRoleSemantic(EmptyRoleSemantic emptyRoleSemantic) {
        this.emptyRoleSemantic = emptyRoleSemantic;
        return this;
    }

    /**
     * Retrieve the empty-role semantic.
     *
     * @return The empty-role semantic.
     */
    public EmptyRoleSemantic getEmptyRoleSemantic() {
        return this.emptyRoleSemantic;
    }

    /**
     * Add an allowed role.
     *
     * @param role The role.
     * @return This meta-data object.
     * @see #setEmptyRoleSemantic(EmptyRoleSemantic)
     */
    public HttpMethodConstraintMetaData addRoleAllowed(String role) {
        this.rolesAllowed.add(role);
        return this;
    }

    /**
     * Retrieve the allowed roles.
     *
     * @return The allowed roles.
     * @see #getEmptyRoleSemantic()
     */
    public Set<String> getRolesAllowed() {
        return this.rolesAllowed;
    }

    /**
     * Set the transport guarantee.
     *
     * @param transportGuarantee The transport guarantee.
     * @return This meta-data object.
     */
    public HttpMethodConstraintMetaData setTransportGuarantee(TransportGuarantee transportGuarantee) {
        this.transportGuarantee = transportGuarantee;
        return this;
    }

    /**
     * Retrieve the transport guarantee.
     *
     * @return The transport guarantee.
     */
    public TransportGuarantee getTransportGuarantee() {
        return this.transportGuarantee;
    }

    private TransportGuarantee transportGuarantee;

    private EmptyRoleSemantic emptyRoleSemantic;

    private String method;

    private Set<String> rolesAllowed = new HashSet<>();
}
