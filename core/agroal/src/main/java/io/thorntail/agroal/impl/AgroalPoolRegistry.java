package io.thorntail.agroal.impl;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.thorntail.agroal.AgroalPoolMetaData;
import org.eclipse.microprofile.config.Config;
import io.thorntail.TraceMode;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.Converter;

/**
 * Created by johara on 10/16/2018.
 */
@ApplicationScoped
public class AgroalPoolRegistry implements Iterable<AgroalPoolMetaData> {

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
            AgroalPoolMetaData ds = create(id, groups.get(id));
            register(ds);
        }
    }

    AgroalPoolMetaData create(String id, List<String> configProps) {
        AgroalPoolMetaData ds = new AgroalPoolMetaData(id);
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
            } else if (simpleName.equals("max-size")) {
                ds.setMaxSize(config.getValue(e, Integer.class));
            } else if (simpleName.equals("min-size")) {
                ds.setMinSize(config.getValue(e, Integer.class));
            } else if (simpleName.equals("initial-size")) {
                ds.setInitialSize(config.getValue(e, Integer.class));
            } else if (simpleName.equals("leak-timeout")) {
                ds.setLeakTimeout(config.getValue(e, Duration.class));
            } else if (simpleName.equals("acquisition-timeout")) {
                ds.setAcquisitionTimeout(config.getValue(e, Duration.class));
            } else if (simpleName.equals("reap-timeout")) {
                ds.setReapTimeout(config.getValue(e, Duration.class));
            } else if (simpleName.equals("validation-timeout")) {
                ds.setValidationTimeout(config.getValue(e, Duration.class));
            } else if (simpleName.equals("validate-connection")) {
                ds.setValidateConnection(config.getValue(e, Boolean.class));
            } else if (simpleName.equals("jta")) {
                ds.setJta(config.getValue(e, Boolean.class));
            } else if (simpleName.equals("xa")) {
                ds.setXa(config.getValue(e, Boolean.class));
            } else {
                AgroalMessages.MESSAGES.unknownConfigParameter(e, config.getValue(e, String.class));
            }
        });

        if (ds.getJNDIName() == null) {
            ds.setJNDIName("java:jboss/datasources/" + ds.getId());
        }

        return ds;

    }

    public void register(AgroalPoolMetaData ds) {
        this.datasources.add(ds);
    }

    @Override
    public Iterator<AgroalPoolMetaData> iterator() {
        return this.datasources.iterator();
    }

    @Inject
    Config config;

    private List<AgroalPoolMetaData> datasources = new ArrayList<>();

    public List<AgroalPoolMetaData> getDatasources() {
        return datasources;
    }
}
