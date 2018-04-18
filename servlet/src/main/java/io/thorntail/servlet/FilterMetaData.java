package io.thorntail.servlet;

import javax.servlet.Filter;

/**
 * Servlet filter descriptor.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class FilterMetaData {

    /**
     * Construct
     *
     * @param name The name of the filter.
     * @param type The class of the filter.
     */
    public FilterMetaData(String name, Class<? extends Filter> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Retrieve the name of the filter.
     *
     * @return The name of the filter.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Retrieve the type of the filter.
     *
     * @return The type of the filter.
     */
    public Class<? extends Filter> getType() {
        return this.type;
    }

    private final String name;

    private final Class<? extends Filter> type;
}
