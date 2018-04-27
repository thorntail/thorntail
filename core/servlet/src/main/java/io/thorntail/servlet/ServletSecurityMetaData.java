package io.thorntail.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;

/**
 * Servlet security descriptor.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class ServletSecurityMetaData {

    /**
     * Construct.
     */
    public ServletSecurityMetaData() {
        this.httpConstraint = new HttpConstraintMetaData();
    }

    /**
     * Construct.
     *
     * @param anno The underlying {@link ServletSecurity} annotation to use for configuration.
     */
    public ServletSecurityMetaData(ServletSecurity anno) {
        this.httpConstraint = new HttpConstraintMetaData(anno.value());

        for (HttpMethodConstraint each : anno.httpMethodConstraints()) {
            addHttpMethodConstraint(new HttpMethodConstraintMetaData(each));
        }
    }

    /**
     * Set the HTTP constraint.
     *
     * @param httpConstraint The HTTP constraint descriptor.
     * @return This meta-data object.
     */
    public ServletSecurityMetaData setHttpConstraint(HttpConstraintMetaData httpConstraint) {
        this.httpConstraint = httpConstraint;
        return this;
    }

    /**
     * Retrieve the HTTP constraint.
     *
     * @return The HTTP constraint descriptor.
     */
    public HttpConstraintMetaData getHttpConstraint() {
        return this.httpConstraint;
    }

    /** Add an HTTP method constraint.
     *
     * @param constraint The HTTP method constraint descriptor.
     * @return This meta-data object.
     */
    public ServletSecurityMetaData addHttpMethodConstraint(HttpMethodConstraintMetaData constraint) {
        this.httpMethodConstraints.add(constraint);
        return this;
    }

    /** Retrieve the HTTP method constriants.
     *
     * @return The HTTP method constraints.
     */
    public List<HttpMethodConstraintMetaData> getHttpMethodConstraints() {
        return this.httpMethodConstraints;
    }

    private HttpConstraintMetaData httpConstraint;

    private List<HttpMethodConstraintMetaData> httpMethodConstraints = new ArrayList<>();
}
