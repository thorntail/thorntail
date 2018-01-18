package org.jboss.unimbus.servlet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;

/**
 * Created by bob on 1/18/18.
 */
public class ServletSecurityMetaData {
    public enum EmptyRoleSemantic {
        PERMIT,
        DENY,
    }

    public enum TransportGuarantee {
        NONE,
        CONFIDENTIAL,
    }

    public ServletSecurityMetaData() {
        this.httpConstraint = new HttpConstraintMetaData();
    }

    public ServletSecurityMetaData(ServletSecurity anno) {
        this.httpConstraint = new HttpConstraintMetaData(anno.value());

        for (HttpMethodConstraint each : anno.httpMethodConstraints()) {
            addHttpMethodConstraint( new HttpMethodConstraintMetaData(each));
        }
    }

    public ServletSecurityMetaData setHttpConstraint(HttpConstraintMetaData httpConstraint) {
        this.httpConstraint = httpConstraint;
        return this;
    }

    public HttpConstraintMetaData getHttpConstraint() {
        return this.httpConstraint;
    }

    public ServletSecurityMetaData addHttpMethodConstraint(HttpMethodConstraintMetaData constraint) {
        this.httpMethodConstraints.add( constraint );
        return this;
    }

    public List<HttpMethodConstraintMetaData> getHttpMethodConstraints() {
        return this.httpMethodConstraints;
    }

    private HttpConstraintMetaData httpConstraint;
    private List<HttpMethodConstraintMetaData> httpMethodConstraints = new ArrayList<>();
}
