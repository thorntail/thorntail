package io.thorntail.servlet;

import java.net.URL;

/**
 * Interface to provide static resources.
 *
 * <p>All available {@code ResourceProvider} components will be searched when a resource is required.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public interface ResourceProvider {

    /** Retrieve a resource by path.
     *
     * @param path The requested path.
     * @return A URL to the requested resource if found, otherwise {@code null}.
     */
    URL getResource(String path);
}
