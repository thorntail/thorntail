package org.jboss.unimbus.servlet;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.annotation.HttpMethodConstraint;

/**
 * Created by bob on 1/18/18.
 */
public class HttpMethodConstraintMetaData {

    public HttpMethodConstraintMetaData(String method) {
        this.method = method;
        this.emptyRoleSemantic = ServletSecurityMetaData.EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = ServletSecurityMetaData.TransportGuarantee.NONE;
    }

    public HttpMethodConstraintMetaData(HttpMethodConstraint anno) {
        this.method = anno.value();

        switch (anno.emptyRoleSemantic()) {
            case PERMIT:
                this.emptyRoleSemantic = ServletSecurityMetaData.EmptyRoleSemantic.PERMIT;
                break;
            case DENY:
                this.emptyRoleSemantic = ServletSecurityMetaData.EmptyRoleSemantic.DENY;
                break;
        }

        for (String role : anno.rolesAllowed()) {
            addRoleAllowed(role);
        }

        switch (anno.transportGuarantee()) {
            case NONE:
                this.transportGuarantee = ServletSecurityMetaData.TransportGuarantee.NONE;
                break;
            case CONFIDENTIAL:
                this.transportGuarantee = ServletSecurityMetaData.TransportGuarantee.CONFIDENTIAL;
                break;
        }
    }

    public HttpMethodConstraintMetaData setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getMethod() {
        return this.method;
    }

    public HttpMethodConstraintMetaData setEmptyRoleSemantic(ServletSecurityMetaData.EmptyRoleSemantic emptyRoleSemantic) {
        this.emptyRoleSemantic = emptyRoleSemantic;
        return this;
    }

    public ServletSecurityMetaData.EmptyRoleSemantic getEmptyRoleSemantic() {
        return this.emptyRoleSemantic;
    }

    public HttpMethodConstraintMetaData addRoleAllowed(String role) {
        this.rolesAllowed.add(role);
        return this;
    }

    public Set<String> getRolesAllowed() {
        return this.rolesAllowed;
    }

    public HttpMethodConstraintMetaData setTransportGuarantee(ServletSecurityMetaData.TransportGuarantee transportGuarantee) {
        this.transportGuarantee = transportGuarantee;
        return this;
    }

    public ServletSecurityMetaData.TransportGuarantee getTransportGuarantee() {
        return this.transportGuarantee;
    }

    private ServletSecurityMetaData.TransportGuarantee transportGuarantee;

    private ServletSecurityMetaData.EmptyRoleSemantic emptyRoleSemantic;

    private String method;

    private Set<String> rolesAllowed = new HashSet<>();
}
