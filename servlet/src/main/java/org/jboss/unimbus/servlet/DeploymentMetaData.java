package org.jboss.unimbus.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.ServletContextListener;

import org.jboss.unimbus.servlet.annotation.Management;

/**
 * Root level servlet deployment descriptor.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class DeploymentMetaData {

    /**
     * Construct
     *
     * @param name The deployment name.
     */
    public DeploymentMetaData(String name) {
        this.name = name;
    }

    /**
     * Retrieve the deployment name.
     *
     * @return The deployment name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the flag indicating the deployment should be bound to the management endpoint.
     *
     * <p>This method may be used instead of applying the {@link Management} annotation.</p>
     *
     * @param management boolean Management endpoint flag.
     * @see Management
     */
    public void setManagement(boolean management) {
        this.management = management;
        if (management && this.realm == null) {
            setRealm("management");
        }
    }

    /**
     * Retrieve the management endpoint flag.
     *
     * @return {code true} if this deployment should be bound to the management endpoint, otherwise {@code false}.
     */
    public boolean isManagement() {
        return this.management;
    }

    /**
     * Set the context path for this deployment.
     *
     * @param contextPath The context path.
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Retrieve the context path for this deployment.
     *
     * @return The context path.
     */
    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * Add a servlet to this deployment.
     *
     * @param servlet The servlet descriptor.
     */
    public void addServlet(ServletMetaData servlet) {
        this.servlets.add(servlet);
    }

    /**
     * Add servlets to this deployment.
     *
     * @param servlets The servlet descriptors.
     */
    public void addServlets(Collection<ServletMetaData> servlets) {
        this.servlets.addAll(servlets);
    }

    /**
     * Add servlets to this deployment.
     *
     * @param servlets The servlet descriptors.
     */
    public void addServlets(Iterable<ServletMetaData> servlets) {
        this.servlets.addAll(
                StreamSupport.stream(servlets.spliterator(), false)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Retrieve all servlets in this deployment.
     *
     * @return The servlet descriptors attached to this deployment.
     */
    public List<ServletMetaData> getServlets() {
        return this.servlets;
    }

    /**
     * Add a servlet context atttribute.
     *
     * @param name  The attribute name.
     * @param value The attribute value.
     */
    public void addServletContextAttribute(String name, Object value) {
        this.servletContextAttributes.put(name, value);
    }

    /**
     * Retrieve the servlet context attributes.
     *
     * @return The servlet context attributes.
     */
    public Map<String, Object> getServletContextAttributes() {
        return this.servletContextAttributes;
    }

    /**
     * Add an init parameter.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     */
    public void addInitParam(String name, String value) {
        this.initParams.put(name, value);
    }

    /**
     * Retrieve the init parameters.
     *
     * @return The init parameters.
     */
    public Map<String, String> getInitParams() {
        return this.initParams;
    }

    /**
     * Set the security realm for this deployment.
     *
     * @param realm The realm name.
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Retrieve the realm name for this deployment.
     *
     * @return The realm name.
     */
    public String getRealm() {
        return this.realm;
    }

    /**
     * Add an authentication method for this deployment.
     *
     * @param authMethod The authentication method.
     */
    public void addAuthMethod(String authMethod) {
        this.authMethods.add(authMethod);
    }

    /**
     * Retrieve all authentication methods for this deployment.
     *
     * @return The authentication methods.
     */
    public List<String> getAuthMethods() {
        return this.authMethods;
    }

    /**
     * Add a filter to this deployment.
     *
     * @param filter The filter descriptor.
     */
    public void addFilter(FilterMetaData filter) {
        this.filters.add(filter);
    }

    /**
     * Retrieve all filters for this deployment.
     *
     * @return The fitlers descriptor.
     */
    public List<FilterMetaData> getFilters() {
        return this.filters;
    }

    /**
     * Add a security constraint to this deployment.
     *
     * @param constraint The security constraint.
     */
    public void addSecurityConstraint(SecurityConstraintMetaData constraint) {
        this.securityConstraints.add(constraint);
    }

    /**
     * Retrieve the security constraints for this deployment.
     *
     * @return The security constraint descriptors.
     */
    public List<SecurityConstraintMetaData> getSecurityConstraints() {
        return this.securityConstraints;
    }

    public void addServletContextListener(ServletContextListener listener) {
        this.servletContextListeners.add( listener );
    }

    public List<ServletContextListener> getServletContextListeners() {
        return this.servletContextListeners;
    }

    public <T> DeploymentMetaData putAttachment(Class<T> cls, T object) {
        this.attachments.put( cls, object );
        return this;
    }

    public <T> T getAttachment(Class<T> cls) {
        return (T) this.attachments.get(cls);
    }


    private String name;

    private boolean management;

    private String contextPath;

    private List<ServletMetaData> servlets = new ArrayList<>();

    private Map<String, String> initParams = new HashMap<>();

    private Map<String, Object> servletContextAttributes = new HashMap<>();

    private String realm;

    private List<String> authMethods = new ArrayList<>();

    private List<FilterMetaData> filters = new ArrayList<>();

    private List<SecurityConstraintMetaData> securityConstraints = new ArrayList<>();

    private List<ServletContextListener> servletContextListeners = new ArrayList<>();

    private Map<Class<?>, Object> attachments = new HashMap<>();
}
