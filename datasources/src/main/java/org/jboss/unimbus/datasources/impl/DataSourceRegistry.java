package org.jboss.unimbus.datasources.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.jboss.unimbus.datasources.DataSourceMetaData;
import org.jboss.unimbus.TraceMode;

/**
 * Created by bob on 2/1/18.
 */
@ApplicationScoped
public class DataSourceRegistry implements Iterable<DataSourceMetaData> {

    public static final String PREFIX = "datasource.";


    @PostConstruct
    void init() {
        Map<String, List<String>> groups = StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                .filter(e -> e.startsWith(PREFIX))
                .sorted()
                .collect(Collectors.groupingBy(e -> {
                             int dotLoc = e.indexOf('.', PREFIX.length());
                             return e.substring(PREFIX.length(), dotLoc);
                         })
                );

        for (String id : groups.keySet()) {
            DataSourceMetaData ds = create(id, groups.get(id));
            register(ds);
        }
    }

    DataSourceMetaData create(String id, List<String> configProps) {
        DataSourceMetaData ds = new DataSourceMetaData(id);
        configProps.forEach(e -> {
            String simpleName = e.substring((PREFIX + id).length() + 1);
            if (simpleName.equals("connection-url")) {
                ds.setConnectionUrl(config.getValue(e, String.class));
            } else if (simpleName.equals("driver")) {
                ds.setDriver(config.getValue(e, String.class));
            } else if (simpleName.equals("username")) {
                ds.setUsername(config.getValue(e, String.class));
            } else if (simpleName.equals("password")) {
                ds.setPassword(config.getValue(e, String.class));
            } else if (simpleName.equals("jndi-name")) {
                ds.setJNDIName(config.getValue(e, String.class));
            } else if (simpleName.equals("trace")) {
                ds.setTraceMode(config.getValue(e, TraceMode.class));
            } else {
                DataSourcesMessages.MESSAGES.unknownConfigParameter(e, config.getValue(e, String.class));
            }
        });

        if (ds.getJNDIName() == null) {
            ds.setJNDIName("java:jboss/datasources/" + ds.getId());
        }

        return ds;

    }

    public void register(DataSourceMetaData ds) {
        this.datasources.add(ds);
    }

    @Override
    public Iterator<DataSourceMetaData> iterator() {
        return this.datasources.iterator();
    }

    @Inject
    Config config;

    private List<DataSourceMetaData> datasources = new ArrayList<>();
}
