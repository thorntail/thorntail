package org.jboss.unimbus.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;

/**
 * Created by bob on 1/18/18.
 */
public class HttpConstraintMetaData {

    public HttpConstraintMetaData() {
        this.emptyRoleSemantic = ServletSecurityMetaData.EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = ServletSecurityMetaData.TransportGuarantee.NONE;
    }

    public HttpConstraintMetaData(HttpConstraint anno) {
        switch (anno.value()) {
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

        switch ( anno.transportGuarantee() ) {
            case NONE:
                this.transportGuarantee = ServletSecurityMetaData.TransportGuarantee.NONE;
                break;
            case CONFIDENTIAL:
                this.transportGuarantee = ServletSecurityMetaData.TransportGuarantee.CONFIDENTIAL;
                break;
        }
    }

    public HttpConstraintMetaData setEmptyRoleSemantic(ServletSecurityMetaData.EmptyRoleSemantic semantic) {
        this.emptyRoleSemantic = semantic;
        return this;
    }

    public ServletSecurityMetaData.EmptyRoleSemantic getEmptyRoleSemantic() {
        return this.emptyRoleSemantic;
    }

    public HttpConstraintMetaData setTransportGuarantee(ServletSecurityMetaData.TransportGuarantee transportGuarantee) {
        this.transportGuarantee = transportGuarantee;
        return this;
    }

    public ServletSecurityMetaData.TransportGuarantee getTransportGuarantee() {
        return this.transportGuarantee;
    }

    public HttpConstraintMetaData addRoleAllowed(String role) {
        this.rolesAllowed.add(role);
        return this;
    }

    public Set<String> getRolesAllowed() {
        return this.rolesAllowed;
    }

    private ServletSecurityMetaData.TransportGuarantee transportGuarantee;
    private ServletSecurityMetaData.EmptyRoleSemantic emptyRoleSemantic;
    private final Set<String> rolesAllowed = new HashSet<>();
}
