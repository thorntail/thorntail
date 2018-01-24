package org.jboss.unimbus.servlet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bob on 1/24/18.
 */
public class WebResourceCollectionMetaData {

    public WebResourceCollectionMetaData() {

    }

    public WebResourceCollectionMetaData addUrlPattern(String pattern) {
        this.urlPatterns.add( pattern );
        return this;
    }

    public Set<String> getUrlPatterns() {
        return this.urlPatterns;
    }

    private Set<String> urlPatterns = new HashSet<>();
}
