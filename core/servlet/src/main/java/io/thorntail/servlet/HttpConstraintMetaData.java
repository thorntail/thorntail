package io.thorntail.servlet;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.annotation.HttpConstraint;

/**
 * An HTTP constraint descriptor.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class HttpConstraintMetaData {

    /**
     * Construct.
     */
    public HttpConstraintMetaData() {
        this.emptyRoleSemantic = EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = TransportGuarantee.NONE;
    }

    /**
     * Construct.
     *
     * @param anno The {@link HttpConstraint} annotation to use for initial configuration.
     */
    public HttpConstraintMetaData(HttpConstraint anno) {
        switch (anno.value()) {
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
     * Set the empty-role semantic.
     *
     * @param semantic The empty-role semantic.
     * @return This meta-data object.
     */
    public HttpConstraintMetaData setEmptyRoleSemantic(EmptyRoleSemantic semantic) {
        this.emptyRoleSemantic = semantic;
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
     * Set the transport guarantee.
     *
     * @param transportGuarantee The transport guarantee.
     * @return This meta-data object.
     */
    public HttpConstraintMetaData setTransportGuarantee(TransportGuarantee transportGuarantee) {
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

    /**
     * Add an allowed role.
     *
     * @param role The role.
     * @return This meta-data object.
     * @see #setEmptyRoleSemantic(EmptyRoleSemantic)
     */
    public HttpConstraintMetaData addRoleAllowed(String role) {
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

    private TransportGuarantee transportGuarantee;

    private EmptyRoleSemantic emptyRoleSemantic;

    private final Set<String> rolesAllowed = new HashSet<>();
}
