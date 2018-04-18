package io.thorntail.logging.impl;

/**
 * Created by bob on 2/15/18.
 */
public class LoggingUtil {
    /**
     * Base logging category.
     */
    public static final String BASE_LOGGER_CATEGORY = "io.thorntail";

    /**
     * Uppercase project code, suitable for logging prefixes.
     */
    public static final String CODE = "THORN-";

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
