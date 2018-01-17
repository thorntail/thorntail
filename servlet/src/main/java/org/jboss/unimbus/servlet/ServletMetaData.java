package org.jboss.unimbus.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.Servlet;

/**
 * Created by bob on 1/17/18.
 */
public class ServletMetaData {

    public ServletMetaData(Class<? extends Servlet> type, Supplier<? extends Servlet> supplier) {
        this.type = type;
        this.supplier = supplier;
    }

    public String getName() {
        return getType().getSimpleName();
    }

    public Class<? extends Servlet> getType() {
        return this.type;
    }

    public Supplier<? extends Servlet> getSupplier() {
        return this.supplier;
    }

    public void addUrlPattern(String pattern) {
        this.urlPatterns.add( pattern );
    }

    public List<String> getUrlPatterns() {
        return this.urlPatterns;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    public boolean isAsyncSupported() {
        return this.asyncSupported;
    }

    public void setLoadOnStartup(Integer loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public Integer getLoadOnStartup() {
        return this.loadOnStartup;
    }

    public void addInitParam(String name, String value) {
        this.initParams.put( name, value );
    }


    public Map<String,String> getInitParams() {
        return this.initParams;
    }

    private final Class<? extends Servlet> type;
    private final Supplier<? extends Servlet> supplier;
    private final List<String> urlPatterns = new ArrayList<>();
    private boolean asyncSupported;
    private Integer loadOnStartup;
    private Map<String,String> initParams = new HashMap<>();
}
