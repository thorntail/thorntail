package org.jboss.unimbus.logging.impl;

import org.jboss.unimbus.Info;

/**
 * Created by bob on 2/15/18.
 */
public class LoggingUtil {
    /**
     * Base logging category.
     */
    public static final String BASE_LOGGER_CATEGORY = "org.jboss.unimbus";

    /**
     * Uppercase project code, suitable for logging prefixes.
     */
    public static final String CODE = "UNIMBUS-";

    /**
     * Create a logging sub-category.
     *
     * @param name The sub-category.
     * @return A string concatenating {@link #BASE_LOGGER_CATEGORY} with the passed-in parameter.
     */
    public static String loggerCategory(String name) {
        return BASE_LOGGER_CATEGORY + "." + name;
    }
}
