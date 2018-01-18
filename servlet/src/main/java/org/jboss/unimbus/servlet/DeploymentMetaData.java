package org.jboss.unimbus.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by bob on 1/17/18.
 */
public class DeploymentMetaData {

    public DeploymentMetaData(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setManagement(boolean management) {
        this.management = management;
        if ( management && this.realm == null ) {
            setRealm("management");
        }
    }

    public boolean isManagement() {
        return this.management;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public void addServlet(ServletMetaData servlet) {
        this.servlets.add(servlet);
    }

    public void addServlets(Collection<ServletMetaData> servlets) {
        this.servlets.addAll(servlets);
    }

    public void addServlets(Iterable<ServletMetaData> servlets) {
        this.servlets.addAll(
                StreamSupport.stream(servlets.spliterator(), false)
                        .collect(Collectors.toList())
        );
    }

    public List<ServletMetaData> getServlets() {
        return this.servlets;
    }

    public void addServletContextAttribute(String name, Object value) {
        this.servletContextAttributes.put(name, value);
    }

    public Map<String, Object> getServletContextAttributes() {
        return this.servletContextAttributes;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getRealm() {
        return this.realm;
    }

    public void addAuthMethod(String authMethod) {
        this.authMethods.add( authMethod );
    }

    public List<String> getAuthMethods() {
        return this.authMethods;
    }

    private String name;

    private boolean management;

    private String contextPath;

    private List<ServletMetaData> servlets = new ArrayList<>();

    private Map<String, Object> servletContextAttributes = new HashMap<>();

    private String realm;

    private List<String> authMethods = new ArrayList<>();
}
