package org.jboss.unimbus.servlet;

import java.net.URL;

/**
 * Created by bob on 1/22/18.
 */
public interface ResourceProvider {

    URL getResource(String path);
}
