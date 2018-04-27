package io.thorntail.jdbc.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.thorntail.jdbc.DriverMetaData;

/**
 * Created by bob on 2/1/18.
 */
@ApplicationScoped
public class JDBCDriverRegistry implements Iterable<DriverMetaData> {

    @PostConstruct
    private void register() {
        for (DriverMetaData driverMetaData : this.driverInfos) {
            register(driverMetaData);
        }
    }

    public JDBCDriverRegistry() {

    }

    public void register(DriverMetaData driver) {
        this.drivers.put( driver.getId(), driver );
        JDBCMessages.MESSAGES.registeredDriver(driver.getId());
    }

    public void unregister(DriverMetaData driver) {
        this.drivers.remove(driver.getId());
    }

    public DriverMetaData get(String id) {
        return this.drivers.get(id);
    }

    @Override
    public Iterator<DriverMetaData> iterator() {
        return this.drivers.values().iterator();
    }

    public int size() {
        return this.drivers.size();
    }

    private Map<String,DriverMetaData> drivers = new HashMap<>();

    @Inject
    @Any
    Instance<DriverMetaData> driverInfos;

}
