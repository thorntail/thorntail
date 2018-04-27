package io.thorntail.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.Servlet;

/**
 * A servlet descriptor.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 * @see DeploymentMetaData
 */
public class ServletMetaData {

    /**
     * Construct.
     *
     * @param name The name of the servlet.
     * @param type The class of the service.
     */
    public ServletMetaData(String name, Class<? extends Servlet> type) {
        this(name, type, () -> {
            try {
                return type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Constuct.
     *
     * @param name    The name of the servlet.
     * @param servlet The servlet instance.
     */
    public ServletMetaData(String name, Servlet servlet) {
        this(name, servlet.getClass(), () -> servlet);
    }

    /**
     * Constuct.
     *
     * @param type     The type of servlet.
     * @param supplier The supplier of servlet instances.
     */
    public ServletMetaData(Class<? extends Servlet> type, Supplier<? extends Servlet> supplier) {
        this(type.getSimpleName(), type, supplier);
    }

    /**
     * Construct.
     *
     * @param name     The name of the servlet.
     * @param type     The type of the servlet.
     * @param supplier The supplier of servlet instances.
     */
    public ServletMetaData(String name, Class<? extends Servlet> type, Supplier<? extends Servlet> supplier) {
        this.name = name;
        this.type = type;
        this.supplier = supplier;
    }

    /**
     * Retrieve the name of the servlet.
     *
     * @return The name.
     */
    public String getName() {
        return getType().getSimpleName();
    }

    /**
     * Retrieve the type of the servlet.
     *
     * @return The type.
     */
    public Class<? extends Servlet> getType() {
        return this.type;
    }

    /**
     * Retrieve the supplier of the servlet instances.
     *
     * @return The supplier.
     */
    public Supplier<? extends Servlet> getSupplier() {
        return this.supplier;
    }

    /**
     * Add a URL pattern for this servlet.
     *
     * @param pattern The URL pattenr.
     */
    public void addUrlPattern(String pattern) {
        this.urlPatterns.add(pattern);
    }

    /**
     * Retrieve the URL patterns for this servlet.
     *
     * @return The URL patterns.
     */
    public List<String> getUrlPatterns() {
        return this.urlPatterns;
    }

    /**
     * Set the flag which determines if async is supported.
     *
     * @param asyncSupported The flag for async support.
     */
    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    /**
     * Retrieve the flag which determines if async is supported.
     *
     * @return {@code true} if async is supported, otherwise {@code false}.
     */
    public boolean isAsyncSupported() {
        return this.asyncSupported;
    }

    /**
     * Set the flag which determines if the servlet should be loaded on startup.
     *
     * @param loadOnStartup The load-on-startup flag.
     */
    public void setLoadOnStartup(Integer loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    /**
     * Retrieve the flag which determines if the servlet should be loaded on startup.
     *
     * @return The load-on-startup flag.
     */
    public Integer getLoadOnStartup() {
        return this.loadOnStartup;
    }

    /**
     * Add an init param.
     *
     * @param name  The name.
     * @param value The value.
     */
    public void addInitParam(String name, String value) {
        this.initParams.put(name, value);
    }

    /**
     * Retrieve the init params.
     *
     * @return The init params.
     */
    public Map<String, String> getInitParams() {
        return this.initParams;
    }

    /**
     * Set the security descriptor for this servlet.
     *
     * @param security The security descriptor.
     */
    public void setSecurity(ServletSecurityMetaData security) {
        this.security = security;
    }

    /**
     * Retrieve the security descriptor for this servlet.
     *
     * @return The security descriptor.
     */
    public ServletSecurityMetaData getSecurity() {
        return this.security;
    }


    private final String name;

    private final Class<? extends Servlet> type;

    private final Supplier<? extends Servlet> supplier;

    private final List<String> urlPatterns = new ArrayList<>();

    private boolean asyncSupported;

    private Integer loadOnStartup;

    private Map<String, String> initParams = new HashMap<>();

    private ServletSecurityMetaData security;

}
