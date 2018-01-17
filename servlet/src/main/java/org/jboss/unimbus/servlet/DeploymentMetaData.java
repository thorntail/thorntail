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

    private String name;

    private boolean management;

    private String contextPath;

    private List<ServletMetaData> servlets = new ArrayList<>();

    private Map<String, Object> servletContextAttributes = new HashMap<>();
}
