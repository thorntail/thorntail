package io.thorntail.servlet;

import java.util.HashSet;
import java.util.Set;

/**
 * Web resource collection descriptor;
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class WebResourceCollectionMetaData {

    /** Construct.
     *
     */
    public WebResourceCollectionMetaData() {

    }

    /** Add a URL pattern.
     *
     * @param pattern The URL pattern.
     * @return This meta-data object.
     */
    public WebResourceCollectionMetaData addUrlPattern(String pattern) {
        this.urlPatterns.add(pattern);
        return this;
    }

    /** Retrieve the URL patterns.
     *
     * @return The URL patterns.
     */
    public Set<String> getUrlPatterns() {
        return this.urlPatterns;
    }

    private Set<String> urlPatterns = new HashSet<>();
}
