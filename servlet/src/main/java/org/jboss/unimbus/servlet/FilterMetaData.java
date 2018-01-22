package org.jboss.unimbus.servlet;

import javax.servlet.Filter;

/**
 * Created by bob on 1/22/18.
 */
public class FilterMetaData {

    public FilterMetaData(String name, Class<? extends Filter> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public Class<? extends Filter> getType() {
        return this.type;
    }

    private final String name;
    private final Class<? extends Filter> type;
}
